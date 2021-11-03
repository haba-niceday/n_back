package mojosurya.n_back

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_n_back.*
import kotlinx.coroutines.*
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import androidx.annotation.RequiresApi


class NBackActivity : AppCompatActivity() {

    // 正解数、音声、ボタンでそれぞれ6問以上の正解を用意する
    private val NUMBER_OF_CORRECT_ANSWERS = 6

    // コルーチン用のスコープ、
    private var mScope = CoroutineScope(Dispatchers.Default)
    private var mIndex = 1
    private var mLoopCounts = 19 // 出題数、nをプラスする
    private var mSleepCounts = 3 // 音声再生、パネル色変更後のユーザ入力待ちのループカウント(3秒)

    private var mNBackNum = 1 // nの番号、初期は1、正解数によって増減
    private var mAllRandomNum = mutableListOf<Int>() // どのパネルの色を変更するかの数字のリスト
    private var mAllRandomSoundNum = mutableListOf<Int>() // どの音声を再生するかの数字のリスト
    private var mIsPushEyeBtn = true // 目ボタンを押せるかどうかのフラグ
    private var mIsPushEarBtn = true // 耳ボタンを押せるかどうかのフラグ
    private var mIsTargeted = false // パネルの色を変更したかどうかのフラグ
    private var mIsBtnColorCleared = true // パネルの色をクリアしたかどうかのフラグ

    private var mEyeCorrectCount = 0
    private var mEyeWrongCount = 0
    private var mEarCorrectCount = 0
    private var mEarWrongCount = 0

    private lateinit var soundPool: SoundPool
    private var soundOne = 0
    private var soundTwo = 0
    private var soundThree = 0
    private var soundFour = 0
    private var soundFive = 0
    private var soundSix = 0
    private var soundSeven = 0
    private var soundEight = 0
    private var soundNine = 0

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_n_back)

        // MainActivity、ResultActivityからnの値はもらう、初期は1
        // ResultActivityで一定の正解数の場合nをプラス1する
        mNBackNum = intent.getIntExtra("NBACK_NUM", 1)
        n_number.text = "n = " + mNBackNum.toString()

        mLoopCounts += mNBackNum

        // 音声をロードする
        loadSound()

        // どの音声を再生するか、どのパネルの色を変えるかの、ランダムな数字を作成する
        mAllRandomNum = createRandomNum(8)
        mAllRandomSoundNum = createRandomNum(8)


        // 目のボタン押下時の処理
        btn_eye.setOnClickListener {
            if (mIsPushEyeBtn) {
                if (mIndex - mNBackNum < 0) {
                    mEyeWrongCount++
                    Log.d("=======eye wrong count", "wrong");
                } else {
                    if (mAllRandomNum[mIndex - 1] === mAllRandomNum[mIndex - 1 - mNBackNum]) {
                        mEyeCorrectCount++
                        Log.d("======eye correct count", "correct");
                    } else {
                        mEyeWrongCount++
                        Log.d("=======eye wrong count", "wrong");
                    }
                }
            }
            mIsPushEyeBtn = false
        }

        // 耳ボタン押下時の処理
        btn_ear.setOnClickListener {
            if (mIsPushEarBtn) {
                if (mIndex - mNBackNum < 0) {
                    mEarWrongCount++
                    Log.d("=======ear wrong count", "wrong");
                } else {
                    if (mAllRandomSoundNum[mIndex - 1] === mAllRandomSoundNum[mIndex - 1 - mNBackNum]) {
                        mEarCorrectCount++
                        Log.d("======ear correct count", "correct");
                    } else {
                        mEarWrongCount++
                        Log.d("=======ear wrong count", "wrong");
                    }
                }
            }
            mIsPushEarBtn = false
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("==========onStop", "onstop");
        mScope.cancel()
    }

    override fun onResume() {
        super.onResume()
        Log.d("Test", "lifecycle : onResume")
        mScope = CoroutineScope(Dispatchers.Default)
        // パネルの色変更、音声再生処理
        mScope.launch {
            nBack()
        }
    }

    /*
    * ランダムな数字を10個生成して、10回ループするなかで、
    * ひとつづつ生成された数字のボタンの色を変える
    * ボタンの色変更は3秒ごとに変更して、変更する前に200ms一瞬全てボタンの色が戻る
    * */
    private suspend fun nBack() {
        try {
            // 画面表示した後に少し間をおく
            Thread.sleep(2000)

            /*
            * ループ処理内で、3秒ごとに音声を生成、パネルの色変更を繰り返す
            * メインスレッドのコルーチンとして実行し、アプリの停止でコルーチンの処理はキャンセルする
            * キャンセルされた後に再度呼び出された時のため、各種フラグで停止状態から実行できるようにする
            */
            for (i in mIndex..mLoopCounts) {
                mIndex = i
                mIsPushEyeBtn = false
                mIsPushEarBtn = false

                Log.d("=============mIndex", mIndex.toString())
                Log.d("===========mLoopCounts", mLoopCounts.toString())

                // 一つ前のボタンの色を元に戻す処理、2回目以降のループで実行
                if (!mIsBtnColorCleared) {
                    withContext(Dispatchers.Main) {
                        clearBtnColor(mAllRandomNum[mIndex - 2])
                        mIsBtnColorCleared = true
                    }
                    mIsTargeted = false
                    Thread.sleep(200)
                }

                // 対象のパネルをターゲット色に変更し、音声を再生する処理
                if (!mIsTargeted) {
                    withContext(Dispatchers.Main) {
                        changeTargetBtnColor(mAllRandomNum[mIndex - 1])
                        soundPool.play(
                            getSoundFromNum(mAllRandomSoundNum[mIndex - 1]),
                            1.0f,
                            1.0f,
                            0,
                            0,
                            1.0f
                        )
                    }
                    mIsTargeted = true
                    mIsPushEyeBtn = true
                    mIsPushEarBtn = true
                }

                // 音声再生、パネル色変更後にユーザー押下待ちのスリープ
                if (0 < mSleepCounts) {
                    var counts = mSleepCounts
                    for (j in 1..counts) {
                        Thread.sleep(1000)
                        mSleepCounts--
                    }
                }

                mSleepCounts = 3
                mIsBtnColorCleared = false
                mIsTargeted = false
            }
            // onPostExecuteメソッドと同等の処理
            withContext(Dispatchers.Main) {
                finishNBack()
            }
        } catch (e: Exception) {
            // onCancelledメソッドと同等の処理
            Log.e(localClassName, "canceled", e)
        }
    }

    /*
     * ループ回数が終了したら自動で正解数、失敗数をリザルト画面に送る
     */
    private fun finishNBack() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("EYE_CORRECT_COUNT", mEyeCorrectCount)
        intent.putExtra("EYE_WRONG_COUNT", mEyeWrongCount)
        intent.putExtra("EAR_CORRECT_COUNT", mEarCorrectCount)
        intent.putExtra("EAR_WRONG_COUNT", mEarWrongCount)
        intent.putExtra("NBACK_NUM", mNBackNum)
        startActivity(intent)
        finish()
    }

    /* ランダムな数字の配列を作成
    * 再生する音声とボタンの色を変える処理で使用する
    * 作成したあと正解数分(NUMBER_OF_CORRECT_ANSWERS)の正解がある確認し、
    * ない場合は再度このメソッドを呼び出す
    * */
    private fun createRandomNum(size: Int): MutableList<Int> {
        // mAllRandomNum = MutableList(10) { (1..8).random() }
        // 1..8はアルファベットの数、音声の数に合わせる
        var correctCounts = 0
        Log.d("===========mloop counts", mLoopCounts.toString())
        var randomNumList = MutableList(mLoopCounts) { (1..size).random() }
        for (i in 0..randomNumList.size - 1) {
            if (1 < i - mNBackNum) {
                if (randomNumList[i] === randomNumList[i - mNBackNum]) {
                    correctCounts++
                }
            }
        }
        if (NUMBER_OF_CORRECT_ANSWERS <= correctCounts && correctCounts <= NUMBER_OF_CORRECT_ANSWERS + 1) {
            Log.d("===========created", randomNumList.toString())
            return randomNumList
        }
        return createRandomNum(size)
    }

    private fun getSoundFromNum(soundNum: Int): Int {
        when (soundNum) {
            1 -> {
                return soundOne
            }
            2 -> {
                return soundTwo
            }
            3 -> {
                return soundThree
            }
            4 -> {
                return soundFour
            }
            5 -> {
                return soundFive
            }
            6 -> {
                return soundSix
            }
            7 -> {
                return soundSeven
            }
            8 -> {
                return soundEight
            }
            9 -> {
                return soundNine
            }
            else -> {
                return soundOne
            }
        }
    }

    private fun clearBtnColor(btnNum: Int) {
        when (btnNum) {
            1 -> {
                btn1.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_top_left
                )
            }
            2 -> {
                btn2.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square
                )
            }
            3 -> {
                btn3.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_top_right
                )
            }
            4 -> {
                btn4.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square
                )
            }
            5 -> {
                btn5.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square
                )
            }
            6 -> {
                btn6.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_bottom_left
                )
            }
            7 -> {
                btn7.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square
                )
            }
            8 -> {
                btn8.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_bottom_right
                )
            }
        }
    }

    private fun changeTargetBtnColor(btnNum: Int) {
        Log.d("===================", "change color")
        Log.d("===================", btnNum.toString())
        when (btnNum) {
            1 -> {
                btn1.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_top_left_target
                )
            }
            2 -> {
                btn2.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square_target
                )
            }
            3 -> {
                btn3.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_top_right_target
                )
            }
            4 -> {
                btn4.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square_target
                )
            }
            5 -> {
                btn5.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square_target
                )
            }
            6 -> {
                btn6.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_bottom_left_target
                )
            }
            7 -> {
                btn7.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_square_target
                )
            }
            8 -> {
                btn8.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.btn_bottom_right_target
                )
            }
        }
    }

    private fun loadSound() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(9)
            .build()

        // wav をロードしておく
        soundOne = soundPool.load(this, R.raw.one, 1)
        soundTwo = soundPool.load(this, R.raw.two, 1)
        soundThree = soundPool.load(this, R.raw.three, 1)
        soundFour = soundPool.load(this, R.raw.four, 1)
        soundFive = soundPool.load(this, R.raw.five, 1)
        soundSix = soundPool.load(this, R.raw.six, 1)
        soundSeven = soundPool.load(this, R.raw.seven, 1)
        soundEight = soundPool.load(this, R.raw.eight, 1)
        soundNine = soundPool.load(this, R.raw.nine, 1)
    }
}