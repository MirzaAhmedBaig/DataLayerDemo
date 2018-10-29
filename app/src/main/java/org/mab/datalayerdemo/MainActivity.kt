package org.mab.datalayerdemo

import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.WorkerThread
import android.util.Log
import android.view.View

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.async
import java.util.HashSet
import java.util.concurrent.ExecutionException

class MainActivity : Activity(), MessageClient.OnMessageReceivedListener {

    private val TAG = MainActivity::class.java.simpleName
    private val START_ACTIVITY_PATH = "/start-dhyana"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mStartActivityBtn.setOnClickListener {
            onStartWearableActivityClick()
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getMessageClient(this).addListener(this)
    }

    public override fun onPause() {
        super.onPause()
        Wearable.getMessageClient(this).removeListener(this)
    }


    override fun onMessageReceived(messageEvent: MessageEvent) {
        msg_text.text = messageEvent.path
        Log.d(TAG, "OnMessageReceived CalledPath : ${messageEvent.path}")
    }


    private fun onStartWearableActivityClick() {
        msg_text.text = "mssage sent"
        Log.d(TAG, "onStartWearableActivity Click")
        async {
            val nodes = getNodes()
            for (node in nodes) {
                sendStartActivityMessage(node)
            }
        }
    }


    @WorkerThread
    private fun sendStartActivityMessage(node: String) {
        val data = "dhyana_QR_CODE".toByteArray()
        val sendMessageTask: Task<Int> = Wearable.getMessageClient(this).sendMessage(node, START_ACTIVITY_PATH, data)
        try {
            val result = Tasks.await(sendMessageTask)
            Log.d(TAG, "Message sent: " + result!!)

        } catch (exception: ExecutionException) {
            Log.e(TAG, "Task failed: $exception")

        } catch (exception: InterruptedException) {
            Log.e(TAG, "Interrupt occurred: $exception")
        }

    }


    @WorkerThread
    private fun getNodes(): Collection<String> {
        val results: HashSet<String> = HashSet<String>()

        val nodeListTask: Task<List<Node>> = Wearable.getNodeClient(applicationContext).connectedNodes
        try {
            val nodes = Tasks.await(nodeListTask)

            for (node in nodes) {
                results.add(node.id)
            }

        } catch (exception: ExecutionException) {
            Log.e(TAG, "Task failed: $exception")

        } catch (exception: InterruptedException) {
            Log.e(TAG, "Interrupt occurred: $exception")
        }

        return results
    }
}
