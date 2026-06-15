package xyz.kiurchv.cull.worker

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import xyz.kiurchv.cull.data.IndexingStore
import xyz.kiurchv.cull.data.PhotoRepository
import xyz.kiurchv.cull.data.db.PhotoHashDao
import xyz.kiurchv.cull.data.db.PhotoMetadataDao
import xyz.kiurchv.cull.domain.PHashEngine

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun photoRepository(): PhotoRepository
    fun photoMetadataDao(): PhotoMetadataDao
    fun photoHashDao(): PhotoHashDao
    fun pHashEngine(): PHashEngine
    fun indexingStore(): IndexingStore
}
