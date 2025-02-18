package myapp.chronify.data.nife

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NifeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(nife: Nife)

    // @Insert(onConflict = OnConflictStrategy.REPLACE)
    // suspend fun insertAll(nifes: List<Nife>)

    @Delete
    suspend fun delete(nife: Nife)

    @Update
    suspend fun update(nife: Nife)

    @Query("SELECT * from Nife WHERE id = :id")
    fun getNifeById(id: Int): Flow<Nife>

    @Query("SELECT * from Nife ORDER BY createdDT DESC")
    fun getAllNifes(): Flow<List<Nife>>

    @Query("SELECT * from Nife ORDER BY isFinished ASC, createdDT DESC")
    fun getAllNifesAsPgSrc(): PagingSource<Int, Nife>

    @Query("SELECT * from Nife WHERE isFinished = 1 ORDER BY createdDT DESC")
    fun getFinishedNifesAsPgSrc(): PagingSource<Int, Nife>

    @Query("SELECT * from Nife WHERE isFinished = 0 ORDER BY createdDT DESC")
    fun getUnfinishedNifesAsPgSrc(): PagingSource<Int, Nife>

    @Query(
        """
        SELECT DISTINCT title 
        FROM Nife 
        WHERE title LIKE '%' || :query || '%' 
        LIMIT :limit
    """
    )
    fun getSimilarTitles(query: String, limit: Int): Flow<List<String>>

    @Query("SELECT * FROM Nife WHERE title = :title AND isFinished = 1 ORDER BY endDT DESC")
    fun getFinishedNifesByTitleAsPgSrc(title: String): PagingSource<Int, Nife>

    @Query("SELECT * FROM Nife WHERE isFinished = 1 ORDER BY endDT DESC")
    fun getFinishedNifesForAllAsPgSrc(): PagingSource<Int, Nife>

    // 带过滤条件的分页（示例：按类型过滤）
    @Query("SELECT * FROM Nife WHERE type = :type ORDER BY createdDT DESC")
    fun pagingSourceByType(type: NifeType): PagingSource<Int, Nife>

    // 动态排序分页（示例：可选择排序字段）
    @Query(
        """
        SELECT * FROM Nife ORDER BY 
        CASE :orderBy
        WHEN 'title' THEN title
        WHEN 'created' THEN createdDT
        ELSE createdDT
        END DESC
    """
    )
    fun pagingSourceWithOrder(orderBy: String): PagingSource<Int, Nife>


}