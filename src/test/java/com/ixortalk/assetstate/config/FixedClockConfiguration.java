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
package com.ixortalk.assetstate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;

import static java.time.ZoneOffset.UTC;

@Configuration
public class FixedClockConfiguration {

    public static final String DATETIME = "2016-01-01T01:00:00.000Z";

    /**
     *
     * used for testing purposed.
     * Corresponds to unix epoc 1451610000
     *
     * GMT: Fri, 01 Jan 2016 01:00:00 GMT
     * Your time zone: 1/1/2016, 2:00:00 AM GMT+1:00
     *
     * @return Fixed clock
     */
    @Bean
    @Primary
    public Clock fixedClock() {
            return Clock.fixed(Instant.parse(DATETIME), UTC);
        }
}
