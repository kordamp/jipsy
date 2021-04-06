/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2013 - 2021 Andres Almiray.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*

Copyright 2008-2020 TOPdesk, the Netherlands

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/

package org.kordamp.jipsy.processor;

/**
 * Stores the result of a check.
 * <p>
 * The result can be {@linkplain CheckResult#OK OK} or some error. If the result is {@linkplain CheckResult#OK OK} then
 * there is no error.
 * <p>
 * The result also contains a message in case if is not {@linkplain CheckResult#OK OK}. If the result is {@linkplain
 * CheckResult#OK OK} then the message is {@code null}.
 */
public final class CheckResult {
    /**
     * The single instance of the class that is not an error. It is guaranteed by the implementation that external code
     * out of this class cannot create an instance that is representing the OK state. It is legit to check {@code state
     * == CheckResult.OK} for error. This can also be checked calling {@linkplain CheckResult#isError()}, which is also
     * the preferred and recommended way.
     */
    public static final CheckResult OK = new CheckResult(null);

    private final boolean error;
    private final String message;

    /**
     * Create a new result, for which {@linkplain #isError()} is {@code null} and contains the error message.
     *
     * @param message describes the error
     * @return the new result object
     */
    public static CheckResult valueOf(String message) {
        if (message == null) {
            throw new NullPointerException("message");
        }
        return new CheckResult(message);
    }

    private CheckResult(String message) {
        error = (message != null);
        this.message = message;
    }

    /**
     * @return {@code true} if this is an error result, and {@code false} if this is not the {@linkplain CheckResult#OK
     * OK} instance of the class.
     */
    public boolean isError() {
        return error;
    }

    /**
     *
     * @return the message that describes the result
     */
    public String getMessage() {
        return message;
    }
}