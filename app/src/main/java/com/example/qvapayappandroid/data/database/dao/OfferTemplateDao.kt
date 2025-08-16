package com.example.qvapayappandroid.data.database.dao

import androidx.room.*
import com.example.qvapayappandroid.data.database.entities.OfferTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferTemplateDao {
    
    @Query("SELECT * FROM offer_templates ORDER BY updatedAt DESC")
    fun getAllTemplates(): Flow<List<OfferTemplateEntity>>
    
    @Query("SELECT * FROM offer_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): OfferTemplateEntity?
    
    @Query("SELECT * FROM offer_templates WHERE name = :name LIMIT 1")
    suspend fun getTemplateByName(name: String): OfferTemplateEntity?
    
    @Query("SELECT * FROM offer_templates WHERE type = :type ORDER BY updatedAt DESC")
    fun getTemplatesByType(type: String): Flow<List<OfferTemplateEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: OfferTemplateEntity): Long
    
    @Update
    suspend fun updateTemplate(template: OfferTemplateEntity)
    
    @Delete
    suspend fun deleteTemplate(template: OfferTemplateEntity)
    
    @Query("DELETE FROM offer_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)
    
    @Query("DELETE FROM offer_templates")
    suspend fun deleteAllTemplates()
    
    @Query("SELECT COUNT(*) FROM offer_templates")
    suspend fun getTemplateCount(): Int
    
    @Query("SELECT * FROM offer_templates WHERE name LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' ORDER BY updatedAt DESC")
    fun searchTemplates(searchQuery: String): Flow<List<OfferTemplateEntity>>
}