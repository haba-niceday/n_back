package mojosurya.n_back.Model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NBackModel : ViewModel() {
    val nBackNum: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    init {
        nBackNum.value = 1
    }
}