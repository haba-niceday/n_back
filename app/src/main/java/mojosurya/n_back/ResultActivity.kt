package mojosurya.n_back

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_result.*


class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        var nBackNum = intent.getIntExtra("NBACK_NUM", 1)
        var eyeCorrectCounts = intent.getIntExtra("EYE_CORRECT_COUNT", 0)
        var eyeWrongCounts = intent.getIntExtra("EYE_WRONG_COUNT", 0)
        var earCorrectCounts = intent.getIntExtra("EAR_CORRECT_COUNT", 0)
        var earWrongCounts = intent.getIntExtra("EAR_WRONG_COUNT", 0)

        val correctCounts = eyeCorrectCounts + earCorrectCounts
        val wrongCounts = eyeWrongCounts + earWrongCounts

        // 9問以上正解でnをプラス1する
        // 正解が4以下で、間違いが5以上で、nをマイナス1する
        if (9 <= correctCounts && wrongCounts <= 3) {
            nBackNum++
            message.text = "おめでとうございます(^o^) <br/>n = " + nBackNum.toString() + " に上がりました。"
        } else if (correctCounts <= 4 && 5 <= wrongCounts && 1 < nBackNum) {
            nBackNum--
            message.text = "残念（ToT） <br/>n = " + nBackNum.toString() + " に下がりました。"
        } else {
            message.text = "おしかったね（^-^） <br/>n = " + nBackNum.toString() + " のままです。"
        }

        correctTvEye.text = eyeCorrectCounts.toString()
        wrongTvEye.text = eyeWrongCounts.toString()
        correctTvEar.text = earCorrectCounts.toString()
        wrongTvEar.text = earWrongCounts.toString()

        btn_next.setOnClickListener {
            val intent = Intent(this, NBackActivity::class.java)
            intent.putExtra("NBACK_NUM", nBackNum)
            startActivity(intent)
            finish()
        }
    }
}