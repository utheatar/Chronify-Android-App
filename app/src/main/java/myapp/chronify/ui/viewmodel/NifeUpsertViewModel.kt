package myapp.chronify.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Update
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import myapp.chronify.data.PreferencesRepository
import myapp.chronify.data.nife.Nife
import myapp.chronify.data.nife.NifeRepository
import myapp.chronify.data.nife.NifeType
import myapp.chronify.ui.element.screen.EditNifeScreenRoute
import java.time.LocalDateTime



abstract class NifeUpsertViewModel(
    protected val repository: NifeRepository,
    protected val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    data class NifeUiState(
        val nife: Nife = Nife(title = ""),
        val isValid: Boolean = false,
        val invalidInfo: String = ""
    )

    /**
     * Holds current [NifeUiState]
     */
    var uiState by mutableStateOf(NifeUiState())
        protected set

    // 公共的偏好设置观察
    // val weekStartFromSunday = preferencesRepository.getPreference(PreferencesKey.DisplayPref.WeekStartFromSunday)
    //     .stateIn(
    //         scope = viewModelScope,
    //         started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
    //         initialValue = false
    //     )

    /**
     * Updates the [uiState] with the value provided in the argument.
     * This method also triggers a validation for input values
     * and any other operation that needs to be done after updating the UI state.
     */
    fun updateUiState(nife: Nife, modifyAfterUpdate: Boolean = true) {
        uiState = uiState.copy(
            nife = nife,
            isValid = validateInput(nife)
        )
        if (modifyAfterUpdate)
            modifyAfterUpdatingUiState()
    }

    /**
     * validates the input values of [Nife] object
     */
    protected fun validateInput(nife: Nife = uiState.nife): Boolean {
        // TODO: Add more validation rules, and update invalidInfo
        return nife.title.isNotBlank()
    }

    // UI状态更新后的操作
    open fun modifyAfterUpdatingUiState() {
    }

    // 抽象保存方法
    abstract suspend fun save(clearAfterSave: Boolean = true)
}

class NifeAddViewModel(
    repository: NifeRepository,
    preferencesRepository: PreferencesRepository,
) : NifeUpsertViewModel(repository, preferencesRepository) {

    // override fun modifyAfterUpdatingUiState() {}

    override suspend fun save(clearAfterSave: Boolean) {
        if (validateInput()) {
            repository.insert(uiState.nife)
        }
        if (clearAfterSave)
            uiState = NifeUiState(nife = Nife(title = ""))

    }


}

class NifeEditViewModel(
    savedStateHandle: SavedStateHandle,
    repository: NifeRepository,
    preferencesRepository: PreferencesRepository,
) : NifeUpsertViewModel(repository, preferencesRepository) {

    private val nifeId: Int = checkNotNull(savedStateHandle[EditNifeScreenRoute.itemIdArg])

    init {
        viewModelScope.launch {
            uiState = NifeUiState(
                nife = repository.getNifeById(nifeId).filterNotNull().first(),
                isValid = true
            )
        }
    }

    // override fun modifyAfterUpdatingUiState() {}

    override suspend fun save(clearAfterSave: Boolean) {
        if (validateInput()) {
            repository.update(uiState.nife)
        }
        if (clearAfterSave)
            uiState = NifeUiState(nife = Nife(title = ""))

    }
}


