/*
 * Copyright (c) 2020 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.vector.app.features.discovery

import com.airbnb.mvrx.Async
import com.airbnb.mvrx.MvRxState
import com.airbnb.mvrx.Uninitialized
import org.matrix.android.sdk.internal.auth.registration.LocalizedFlowDataLoginTerms

data class DiscoverySettingsState(
        val identityServer: Async<IdentityServerWithTerms?> = Uninitialized,
        val emailList: Async<List<PidInfo>> = Uninitialized,
        val phoneNumbersList: Async<List<PidInfo>> = Uninitialized,
        // Can be true if terms are updated
        val termsNotSigned: Boolean = false,
        val userConsent: Boolean = false,
        val isIdentityPolicyUrlsExpanded: Boolean = false
) : MvRxState

data class IdentityServerWithTerms(
        val serverUrl: String,
        val policyUrls: List<String>
)
