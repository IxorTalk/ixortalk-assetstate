/**
 *
 *  2016 (c) IxorTalk CVBA
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of IxorTalk CVBA
 *
 * The intellectual and technical concepts contained
 * herein are proprietary to IxorTalk CVBA
 * and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from IxorTalk CVBA.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.
 */
package com.ixortalk.assetstate.domain.aspect;

public enum AspectStatusResolverStrategy implements AspectStatusResolver {

    BOOLEAN_1_OK {
        @Override
        public Status resolveStatus(String aspectValue) {
            switch (aspectValue) {
                case "1":
                    return Status.OK;
                case Aspect.EMPTY_VALUE:
                    return Status.UNKNOWN;
                default:
                    return Status.ERROR;
            }
        }
    },

    BOOLEAN_0_OK {
        @Override
        public Status resolveStatus(String aspectValue) {
            switch (aspectValue) {
                case "0":
                    return Status.OK;
                case Aspect.EMPTY_VALUE:
                    return Status.UNKNOWN;
                default:
                    return Status.ERROR;
            }
        }
    },

    NUMBER_GREATHER_THEN_50 {

        @Override
        public Status resolveStatus(String aspectValue) {

            if (Aspect.EMPTY_VALUE.equals(aspectValue)) {
                return Status.UNKNOWN;
            } else if (Integer.parseInt(aspectValue) > 50) {
                return Status.ERROR;
            } else {
                return Status.OK;
            }
        }
    }
}
