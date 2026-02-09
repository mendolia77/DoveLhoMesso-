package com.dovelhomesso.app

import android.app.Application
import com.dovelhomesso.app.data.db.AppDatabase
import com.dovelhomesso.app.data.entities.*
import com.dovelhomesso.app.data.repositories.AppRepository
import com.dovelhomesso.app.util.SpotCodeGenerator
import com.dovelhomesso.app.workers.ExpiryCheckWorker
import com.dovelhomesso.app.util.NotificationHelper
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DoveLhoMessoApp : Application() {
    
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { 
        AppRepository(
            database.houseRoomDao(),
            database.containerDao(),
            database.spotDao(),
            database.itemDao(),
            database.documentDao()
        )
    }
    
    override fun onCreate() {
        super.onCreate()
        
        try {
            // Create Notification Channel
            NotificationHelper.createNotificationChannel(this)
            
            // Schedule Expiry Check Worker
            setupPeriodicWork()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Demo data disabled - user will create their own data
        // seedDemoDataIfNeeded()
    }
    
    private fun setupPeriodicWork() {
        try {
            val workRequest = PeriodicWorkRequestBuilder<ExpiryCheckWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS) // Run first check after 1 hour to avoid startup lag
                .build()
                
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ExpiryCheck",
                ExistingPeriodicWorkPolicy.KEEP, // Keep existing schedule if app restarts
                workRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun seedDemoDataIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getInstance(this@DoveLhoMessoApp)
            val roomDao = database.houseRoomDao()
            
            // Check if database is empty
            if (roomDao.getRoomCount() > 0) {
                return@launch
            }
            
            // Seed demo data
            val containerDao = database.containerDao()
            val spotDao = database.spotDao()
            val itemDao = database.itemDao()
            val documentDao = database.documentDao()
            
            // Create rooms
            val cameraId = roomDao.insertRoom(HouseRoomEntity(name = "Camera da letto"))
            val cucinaId = roomDao.insertRoom(HouseRoomEntity(name = "Cucina"))
            val studioId = roomDao.insertRoom(HouseRoomEntity(name = "Studio"))
            val garageId = roomDao.insertRoom(HouseRoomEntity(name = "Garage"))
            
            // Create containers for Camera
            val armadioId = containerDao.insertContainer(
                ContainerEntity(
                    roomId = cameraId,
                    name = "Armadio grande",
                    type = ContainerType.ARMADIO.name
                )
            )
            val cassettieraId = containerDao.insertContainer(
                ContainerEntity(
                    roomId = cameraId,
                    name = "Cassettiera",
                    type = ContainerType.CASSETTO.name
                )
            )
            
            // Create containers for Cucina
            val credenzaId = containerDao.insertContainer(
                ContainerEntity(
                    roomId = cucinaId,
                    name = "Credenza",
                    type = ContainerType.SCAFFALE.name
                )
            )
            
            // Create containers for Studio
            val scrivaniaId = containerDao.insertContainer(
                ContainerEntity(
                    roomId = studioId,
                    name = "Scrivania",
                    type = ContainerType.CASSETTO.name,
                    isFavorite = true
                )
            )
            val scaffaleLibriId = containerDao.insertContainer(
                ContainerEntity(
                    roomId = studioId,
                    name = "Scaffale libri",
                    type = ContainerType.SCAFFALE.name
                )
            )
            val cartellinaDocId = containerDao.insertContainer(
                ContainerEntity(
                    roomId = studioId,
                    name = "Cartellina documenti",
                    type = ContainerType.CARTELLINA.name,
                    isFavorite = true
                )
            )
            
            // Create containers for Garage
            val scaffaleAttrezziId = containerDao.insertContainer(
                ContainerEntity(
                    roomId = garageId,
                    name = "Scaffale attrezzi",
                    type = ContainerType.SCAFFALE.name
                )
            )
            
            // Helper to create spots with unique codes
            suspend fun createSpot(
                containerId: Long,
                label: String,
                roomName: String,
                containerName: String,
                isFavorite: Boolean = false
            ): Long {
                val existingCodes = spotDao.getCodesWithPrefix("")
                val code = SpotCodeGenerator.generateCode(roomName, containerName, label, existingCodes)
                return spotDao.insertSpot(
                    SpotEntity(
                        containerId = containerId,
                        label = label,
                        code = code,
                        isFavorite = isFavorite
                    )
                )
            }
            
            // Create spots
            val armadioC1 = createSpot(armadioId, "Cassetto 1", "Camera da letto", "Armadio grande")
            val armadioC2 = createSpot(armadioId, "Cassetto 2", "Camera da letto", "Armadio grande")
            val armadioMensola = createSpot(armadioId, "Mensola alta", "Camera da letto", "Armadio grande")
            
            val cassettiera1 = createSpot(cassettieraId, "Primo cassetto", "Camera da letto", "Cassettiera")
            val cassettiera2 = createSpot(cassettieraId, "Secondo cassetto", "Camera da letto", "Cassettiera")
            
            val credenzaRipiano = createSpot(credenzaId, "Ripiano centrale", "Cucina", "Credenza")
            
            val scrivaniaCassetto = createSpot(scrivaniaId, "Cassetto principale", "Studio", "Scrivania", isFavorite = true)
            val scrivaniaPortaPenne = createSpot(scrivaniaId, "Porta penne", "Studio", "Scrivania")
            
            val scaffaleR1 = createSpot(scaffaleLibriId, "Ripiano 1", "Studio", "Scaffale libri")
            val scaffaleR2 = createSpot(scaffaleLibriId, "Ripiano 2", "Studio", "Scaffale libri")
            
            val cartellinaPersonali = createSpot(cartellinaDocId, "Documenti personali", "Studio", "Cartellina documenti", isFavorite = true)
            val cartellinaCasa = createSpot(cartellinaDocId, "Documenti casa", "Studio", "Cartellina documenti")
            val cartellinaAuto = createSpot(cartellinaDocId, "Documenti auto", "Studio", "Cartellina documenti")
            
            val scaffaleAttr1 = createSpot(scaffaleAttrezziId, "Ripiano attrezzi manuali", "Garage", "Scaffale attrezzi")
            val scaffaleAttr2 = createSpot(scaffaleAttrezziId, "Ripiano elettroutensili", "Garage", "Scaffale attrezzi")
            
            // Create items
            itemDao.insertItem(
                ItemEntity(
                    name = "Maglioni invernali",
                    spotId = armadioC1,
                    category = "Abbigliamento",
                    tags = "inverno, lana"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Coperte extra",
                    spotId = armadioMensola,
                    category = "Casa",
                    keywords = "coperta letto"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Caricatore portatile",
                    spotId = cassettiera1,
                    category = "Elettronica",
                    tags = "usb, powerbank"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Cuffie bluetooth",
                    spotId = cassettiera1,
                    category = "Elettronica",
                    tags = "audio, wireless"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Tovaglioli di stoffa",
                    spotId = credenzaRipiano,
                    category = "Cucina"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Penne e matite",
                    spotId = scrivaniaPortaPenne,
                    category = "Cancelleria"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Chiavette USB",
                    spotId = scrivaniaCassetto,
                    category = "Elettronica",
                    keywords = "usb, memoria, backup"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Cacciaviti set",
                    spotId = scaffaleAttr1,
                    category = "Attrezzi",
                    tags = "fai da te"
                )
            )
            itemDao.insertItem(
                ItemEntity(
                    name = "Trapano",
                    spotId = scaffaleAttr2,
                    category = "Elettroutensili",
                    tags = "fai da te, elettrico"
                )
            )
            
            // Create documents
            documentDao.insertDocument(
                DocumentEntity(
                    title = "Carta d'identità",
                    spotId = cartellinaPersonali,
                    docType = "Documento identità",
                    person = "Mario Rossi",
                    expiryDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000 * 3) // 3 years
                )
            )
            documentDao.insertDocument(
                DocumentEntity(
                    title = "Passaporto",
                    spotId = cartellinaPersonali,
                    docType = "Documento identità",
                    person = "Mario Rossi",
                    expiryDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000 * 5) // 5 years
                )
            )
            documentDao.insertDocument(
                DocumentEntity(
                    title = "Contratto affitto",
                    spotId = cartellinaCasa,
                    docType = "Contratto",
                    tags = "casa, affitto"
                )
            )
            documentDao.insertDocument(
                DocumentEntity(
                    title = "Bollette utenze 2024",
                    spotId = cartellinaCasa,
                    docType = "Bolletta",
                    tags = "luce, gas, acqua"
                )
            )
            documentDao.insertDocument(
                DocumentEntity(
                    title = "Libretto auto",
                    spotId = cartellinaAuto,
                    docType = "Documento veicolo",
                    tags = "automobile"
                )
            )
            documentDao.insertDocument(
                DocumentEntity(
                    title = "Assicurazione auto",
                    spotId = cartellinaAuto,
                    docType = "Polizza",
                    expiryDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000) // 1 year
                )
            )
            documentDao.insertDocument(
                DocumentEntity(
                    title = "Garanzia TV",
                    spotId = scrivaniaCassetto,
                    docType = "Garanzia",
                    expiryDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000 * 2), // 2 years
                    note = "TV Samsung 55 pollici acquistata gennaio 2024"
                )
            )
        }
    }
}
