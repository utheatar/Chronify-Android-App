package myapp.chronify.data.nife

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDateTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import myapp.chronify.R.string
import myapp.chronify.R.drawable
import java.time.ZoneId


/**
 * Nife 实体类
 * @property id 主键，自动生成
 * @property title 标题，非空
 * @property type 类型，默认为 DEFAULT
 * @property isFinished 标记是否完成
 * @property createdDT 创建时间，非空，默认为当前时间
 * @property beginDT 开始时间，可空
 * @property endDT 结束时间，可空
 * @property period 周期类型，可空
 * @property periodMultiple 周期倍数，表示每隔多少周期执行一次，默认为 1。通常实际周期为 [period] * [periodMultiple]
 * @property triggerTimes 触发时间，表示在周期内的哪些时间点执行。emptySet 表示每个周期都执行。
 * @property description 事件的详细描述
 * @property location 地点
 */
@Entity
data class Nife(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "DEFAULT",
    val type: NifeType = NifeType.DEFAULT,
    val isFinished: Boolean = false,
    val createdDT: LocalDateTime = LocalDateTime.now(),
    val beginDT: LocalDateTime? = null,
    val endDT: LocalDateTime? = null,
    // for cyclical schedule
    val period: PeriodType? = null,
    val periodMultiple: Int = 1,
    val triggerTimes: Set<Int> = emptySet(),
    // extra info
    val description: String = "",
    val location: String = "",
)

/**
 * Nife 类型
 * @property RECORD 记录，通常带有开始和结束时间
 * @property DEFAULT 默认，没有特殊属性
 * @property REMINDER 提醒，只有开始时间
 * @property CYCLICAL 周期性，有开始时间和周期，可能有特定的触发时间
 */
enum class NifeType {
    RECORD,
    DEFAULT,
    REMINDER,
    CYCLICAL,
}

@Composable
fun NifeType.getLocalizedName(): String {
    return when (this) {
        NifeType.RECORD -> stringResource(string.schedule_type_record)
        NifeType.DEFAULT -> stringResource(string.schedule_type_default)
        NifeType.REMINDER -> stringResource(string.schedule_type_reminder)
        NifeType.CYCLICAL -> stringResource(string.schedule_type_cyclical)
        else -> stringResource(string.schedule_type_undefined)
    }
}

fun NifeType.getIcon(): Int {
    return when (this) {
        NifeType.RECORD -> drawable.event_available_24px
        NifeType.DEFAULT -> drawable.calendar_add_on_24px
        NifeType.REMINDER -> drawable.event_note_24px
        NifeType.CYCLICAL -> drawable.event_repeat_24dp_e8eaed_fill0_wght400_grad0_opsz24
        else -> drawable.event_note_24px
    }
}

/**
 * 周期类型
 * @property DAILY 每天
 * @property WEEKLY 每周
 * @property MONTHLY 每月
 * @property YEARLY 每年
 */
enum class PeriodType {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

/**
 * Nife 数据库转换器，用于 Room 数据库的数据类型转换
 * @property gson Gson 对象，用于序列化和反序列化
 * @property localDateTimeToTimestamp 将 LocalDateTime 转换为时间戳，LocalDateTime 可精确到纳秒，而时间戳只精确到毫秒，中间会有精度损失。
 * @see androidx.room.TypeConverter
 * @see com.google.gson.Gson
 */
class NifeConverters {
    private val gson = Gson()

    /* LocalDateTime 转换器 */
    @TypeConverter
    fun localDateTimeToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun timestampToLocalDateTime(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(it),
                ZoneId.systemDefault()
            )
        }
    }

    /* PeriodType 枚举转换器 */
    @TypeConverter
    fun periodTypeToString(type: PeriodType?): String? {
        return type?.name
    }

    @TypeConverter
    fun stringToPeriodType(value: String?): PeriodType? {
        return value?.let { PeriodType.valueOf(it) }
    }

    /* NifeType 枚举转换器 */
    @TypeConverter
    fun nifeTypeToString(type: NifeType?): String? {
        return type?.name
    }

    @TypeConverter
    fun stringToNifeType(value: String?): NifeType? {
        return value?.let { NifeType.valueOf(it) }
    }

    /* Set<Int> 集合转换器 */
    @TypeConverter
    fun intSetToJson(set: Set<Int>?): String? {
        return set?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun jsonToIntSet(json: String?): Set<Int>? {
        return json?.let {
            gson.fromJson(it, object : TypeToken<Set<Int>>() {}.type)
        } ?: emptySet()
    }
}