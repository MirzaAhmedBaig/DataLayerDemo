package org.mab.wearapp

import android.content.Intent
import android.os.AsyncTask
import android.support.annotation.WorkerThread
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import java.util.HashSet
import java.util.concurrent.ExecutionException

class DataLayerListenerService : WearableListenerService() {
    private val TAG = "DataLayerService"
    private val START_ACTIVITY_PATH = "/start-dhyana"
    private val ACK_PATH = "MSG_RECEIVED"
    override fun onDataChanged(dataEvents: DataEventBuffer?) {
        Log.d(TAG, "onDataChanged: $dataEvents")
    }

    override fun onMessageReceived(messageEvent: MessageEvent?) {
        Log.d(TAG, "onMessageReceived: " + String(messageEvent!!.data))
        if (messageEvent.path == START_ACTIVITY_PATH) {
            StartWearableActivityTask().execute()
            val startIntent = Intent(this, MainActivity::class.java)
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(startIntent)
        }
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
