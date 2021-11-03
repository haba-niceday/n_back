package mojosurya.n_back

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start.setOnClickListener { v ->
            val intent = Intent(this, NBackActivity::class.java)
            intent.putExtra("NBACK_NUM", 1)
            startActivity(intent)
            finish()
        }
    }

    /*
    * 正解数が8割を超えたらnをプラス1する OK
    * 画面を閉じた場合処理を終了する OK
    * 画面をバックした場合、処理を終了する
    * 正解率によってnをプラス1する OK
    * n = 5くらいまでできるようにしてみる OK
    * 広告をいれてみる
    * 画面の回転対応
    * 文字をstringsに移す
    * ミスカウント(見逃し)を追加する
    * Resultの改行が反映されていない
    * result画面のonce againは、nが上がった場合などは違和感
    * resultのコメント欄のマージンを左右もたす
    * 耳ボタンと、目ボタンが小さいし、見づらい
    *
    * */
}