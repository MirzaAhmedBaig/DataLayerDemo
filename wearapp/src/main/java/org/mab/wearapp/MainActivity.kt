package org.mab.wearapp

import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.WorkerThread
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashSet
import java.util.concurrent.ExecutionException



class MainActivity : FragmentActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val ACK_PATH = "MSG_RECEIVED"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        start_button.setOnClickListener {
            onStartWearableActivityClick()
        }
    }



    private fun onStartWearableActivityClick() {
        Log.d(TAG, "onStartWearableActivity Click")
        StartWearableActivityTask().execute()
    }


    @WorkerThread
    private fun sendStartActivityMessage(node: String) {
        val sendMessageTask: Task<Int> = Wearable.getMessageClient(this).sendMessage(node, ACK_PATH, ByteArray(0))
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
        val results: HashSet<String> = HashSet()

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

    private inner class StartWearableActivityTask : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg args: Void): Void? {
            val nodes = getNodes()
            for (node in nodes) {
                Log.d(TAG,"Node : $node")
                sendStartActivityMessage(node)
            }
            return null
        }
    }

}
