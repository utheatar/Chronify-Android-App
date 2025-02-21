package myapp.chronify.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myapp.chronify.data.nife.NifeRepository
import java.io.OutputStreamWriter
import java.time.format.DateTimeFormatter

class NifeExporter(
    private val context: Context,
    private val repository: NifeRepository
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    suspend fun exportToCsv(uri: Uri) {
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // 写入CSV头
                    writer.write("ID,标题,类型,是否完成,创建时间,开始时间,结束时间,周期类型,周期倍数,触发时间,描述,地点\n")

                    // 获取所有Nife数据
                    repository.getAllNifes().collect { nifes ->
                        nifes.forEach { nife ->
                            writer.write(
                                buildString {
                                    append(nife.id).append(",")
                                    append("\"${nife.title.replace("\"", "\"\"")}\"").append(",")
                                    append(nife.type).append(",")
                                    append(nife.isFinished).append(",")
                                    append(nife.createdDT.format(dateFormatter)).append(",")
                                    append(nife.beginDT?.format(dateFormatter) ?: "").append(",")
                                    append(nife.endDT?.format(dateFormatter) ?: "").append(",")
                                    append(nife.period?.name ?: "").append(",")
                                    append(nife.periodMultiple).append(",")
                                    append("\"${nife.triggerTimes.joinToString(";")}\"").append(",")
                                    append("\"${nife.description.replace("\"", "\"\"")}\"").append(",")
                                    append("\"${nife.location.replace("\"", "\"\"")}\"")
                                    append("\n")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
