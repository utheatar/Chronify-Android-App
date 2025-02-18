package myapp.chronify.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import myapp.chronify.data.PreferencesRepository
import myapp.chronify.data.nife.Nife
import myapp.chronify.data.nife.NifeRepository
import myapp.chronify.ui.element.screen.EditNifeScreenRoute


abstract class NifeUpsertViewModel(
    protected val repository: NifeRepository,
    protected val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    data class NifeUiState(
        val nife: Nife = Nife(title = ""),
        val isValid: Boolean = false,
        val invalidInfo: String = "",
        val suggestedTitles: List<String> = emptyList()
    )

    /**
     * Holds current [NifeUiState]
     */
    protected val _uiState = MutableStateFlow(NifeUiState())
    val uiState: StateFlow<NifeUiState> = _uiState.asStateFlow()

    /**
     * Updates the [uiState] with the value provided in the argument.
     * This method also triggers a validation for input values
     * and any other operation that needs to be done after updating the UI state.
     */
    fun updateUiState(nife: Nife, modifyAfterUpdate: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { current ->
                current.copy(
                    nife = nife,
                    isValid = validateInput(nife),
                    suggestedTitles =
                    if (nife.title.isNotBlank()) {
                        repository.getSimilarTitles(nife.title).first()
                    } else {
                        emptyList()
                    }
                )
            }
            if (modifyAfterUpdate) modifyAfterUpdatingUiState()
        }
    }


    /**
     * validates the input values of [Nife] object
     */
    protected fun validateInput(nife: Nife = _uiState.value.nife): Boolean {
        // TODO: Add more validation rules, and update invalidInfo
        return nife.title.isNotBlank()
    }

    // UI状态更新后的操作
    open fun modifyAfterUpdatingUiState() {
    }

    // 重置 UI 状态
    protected open fun resetUiState() {
        _uiState.update {
            it.copy(
                nife = Nife(title = "", beginDT = null, endDT = null),
                isValid = false,
                invalidInfo = "",
                suggestedTitles = emptyList(),
            )
        }
    }

    // 抽象保存方法
    abstract suspend fun save(resetAfterSave: Boolean = true)


    // 公共的偏好设置观察
    // val weekStartFromSunday =
    // preferencesRepository.getPreference(PreferencesKey.DisplayPref.WeekStartFromSunday)
    //     .stateIn(
    //         scope = viewModelScope,
    //         started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
    //         initialValue = false
    //     )
}

class NifeAddViewModel(
    repository: NifeRepository,
    preferencesRepository: PreferencesRepository,
) : NifeUpsertViewModel(repository, preferencesRepository) {

    // override fun modifyAfterUpdatingUiState() {}

    override suspend fun save(resetAfterSave: Boolean) {
        if (validateInput()) {
            repository.insert(uiState.value.nife)
        }
        if (resetAfterSave)
            resetUiState()
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
            val nife = repository.getNifeById(nifeId).filterNotNull().first()
            _uiState.value = NifeUiState(nife = nife, isValid = true)
        }
    }

    // override fun modifyAfterUpdatingUiState() {}

    override suspend fun save(resetAfterSave: Boolean) {
        if (validateInput()) {
            repository.update(uiState.value.nife)
        }
    }
}


