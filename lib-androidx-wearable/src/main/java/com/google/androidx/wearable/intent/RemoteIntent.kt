/*
 * Copyright 2020 Baptiste Candellier
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.google.androidx.wearable.intent

import android.content.Context
import android.content.Intent
import android.os.Parcel
import android.os.ResultReceiver

object RemoteIntent {

    const val ACTION_REMOTE_INTENT = "com.google.android.wearable.intent.action.REMOTE_INTENT"

    const val EXTRA_INTENT = "com.google.android.wearable.intent.extra.INTENT"
    const val EXTRA_NODE_ID = "com.google.android.wearable.intent.extra.NODE_ID"
    const val EXTRA_RESULT_RECEIVER = "com.google.android.wearable.intent.extra.RESULT_RECEIVER"

    const val RESULT_OK = 0
    const val RESULT_FAILED = 1

    fun startRemoteActivity(
        context: Context,
        intent: Intent,
        resultReceiver: ResultReceiver,
        nodeId: String? = null
    ) {
        require(intent.action == Intent.ACTION_VIEW) {
            "Only ${Intent.ACTION_VIEW} action is currently supported for starting a remote activity"
        }

        requireNotNull(intent.data) {
            "Data Uri is required when starting a remote activity"
        }

        require(intent.categories.contains(Intent.CATEGORY_BROWSABLE)) {
            "The category ${Intent.CATEGORY_BROWSABLE} must be present on the intent"
        }

        context.sendBroadcast(
            (Intent("com.google.android.wearable.intent.action.REMOTE_INTENT"))
                .setPackage("com.google.android.wearable.app")
                .putExtra("com.google.android.wearable.intent.extra.INTENT", intent)
                .putExtra("com.google.android.wearable.intent.extra.NODE_ID", nodeId)
                .putExtra(
                    "com.google.android.wearable.intent.extra.RESULT_RECEIVER",
                    getResultReceiverForSending(resultReceiver)
                )
        )
    }

    private fun getResultReceiverForSending(receiver: ResultReceiver) =
        Parcel.obtain().use {
            receiver.writeToParcel(this, 0)
            setDataPosition(0)
            ResultReceiver.CREATOR.createFromParcel(this) as ResultReceiver
        }
}
