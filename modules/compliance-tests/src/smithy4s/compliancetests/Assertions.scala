/*
 *  Copyright 2021-2022 Disney Streaming
 *
 *  Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     https://disneystreaming.github.io/TOST-1.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package smithy4s.compliancetests

import ComplianceTest._

case class ComplianceTest[F[_]](name: String, run: F[ComplianceResult])

object ComplianceTest {
  type ComplianceResult = Either[String, Unit]
}

object assert {
  def success: ComplianceResult = Right(())
  def fail(msg: String): ComplianceResult = Left(msg)

  def eql[A](expected: A, actual: A): ComplianceResult = {
    if (expected == actual) {
      success
    } else {
      fail(s"Actual value: $actual was not equal to $expected.")
    }
  }
}
