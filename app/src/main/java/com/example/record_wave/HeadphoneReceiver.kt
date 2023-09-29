import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast


class HeadphoneReceiver(private val onHeadphonePlugged: () -> Unit, private val onHeadphoneUnplugged: () -> Unit) : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_HEADSET_PLUG) {
            val state = intent.getIntExtra("state", -1)
            when (state) {
                0 -> {
                    // 耳机拔出
                    Toast.makeText(context, "有线耳机已拔出", Toast.LENGTH_SHORT).show()
                    onHeadphoneUnplugged.invoke()
                }
                1 -> {

                    Toast.makeText(context, "有线耳机已插入", Toast.LENGTH_SHORT).show()
                    // 耳机插入
                    onHeadphonePlugged.invoke()
                    // 执行相关操作
                }
            }
        }
    }
}