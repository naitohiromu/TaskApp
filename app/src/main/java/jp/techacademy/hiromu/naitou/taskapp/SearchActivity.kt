package jp.techacademy.hiromu.naitou.taskapp
import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import jp.techacademy.hiromu.naitou.taskapp.databinding.ActivitySearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var realm: Realm
    private lateinit var task: Task2
    private var calendar = Calendar.getInstance(Locale.JAPANESE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // アクションバーの設定

        setSupportActionBar(binding.toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }


        // EXTRA_TASKからTaskのidを取得
        val intent = intent
        val taskCategory = intent.getStringExtra(EXTRA_TASK)

        // Realmデータベースとの接続を開く
        val config = RealmConfiguration.create(schema = setOf(Task2::class))
        realm = Realm.open(config)
        // TaskAdapterを生成し、ListViewに設定する
        taskAdapter = TaskAdapter(this)
        binding.listView.adapter = taskAdapter
        var contentstest:String? = intent.getStringExtra(EXTRA_TASK)

        val tasktest: RealmResults<Task2> = realm.query<Task2>().find()
        var searchid : Int? = null
        for(task_t in tasktest){
            if(task_t.contents == contentstest){
                searchid = task_t.id
            }
        }

        //Log.d("Android","contents"+contentstest)
        // Realmからタスクの一覧を取得
        val tasks = realm.query<Task2>("id==${searchid}").find()

        // Realmが起動、または更新（追加、変更、削除）時にreloadListViewを実行する
        CoroutineScope(Dispatchers.Default).launch {
            tasks.asFlow().collect {
                when (it) {
                    // 更新時
                    is UpdatedResults -> reloadListView(it.list)
                    // 起動時
                    is InitialResults -> reloadListView(it.list)
                    else -> {}
                }
            }
        }
    }

    private suspend fun reloadListView(tasks: List<Task2>) {
        withContext(Dispatchers.Main) {
            taskAdapter.updateTaskList(tasks)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        // Realmデータベースとの接続を閉じる
        realm.close()
    }
}