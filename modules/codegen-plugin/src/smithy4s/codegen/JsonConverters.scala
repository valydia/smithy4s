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

package smithy4s.codegen

import sjsonnew._
import BasicJsonProtocol._
import sbt.FileInfo
import sbt.HashFileInfo
import sjsonnew._

// Json codecs used by SBT's caching constructs
private[smithy4s] object JsonConverters {

  // This serialises a path by providing a hash of the content it points to.
  // Because the hash is part of the Json, this allows SBT to detect when a file
  // changes and invalidate its relevant caches, leading to a call to Smithy4s' code generator.
  implicit val pathFormat: JsonFormat[os.Path] =
    BasicJsonProtocol.projectFormat[os.Path, HashFileInfo](
      p => FileInfo.hash(p.toIO),
      hash => os.Path(hash.file)
    )

  // format: off
  type GenTarget = List[os.Path] :*: os.Path :*: os.Path :*: Boolean :*: Boolean :*: Boolean :*: Option[Set[String]] :*: Option[Set[String]] :*: List[String] :*: List[String] :*: List[String] :*: List[os.Path] :*: LNil
  // format: on
  implicit val codegenArgsIso = LList.iso[CodegenArgs, GenTarget](
    { ca: CodegenArgs =>
      ("specs", ca.specs) :*:
        ("output", ca.output) :*:
        ("openapiOutput", ca.openapiOutput) :*:
        ("skipScala", ca.skipScala) :*:
        ("skipOpenapi", ca.skipOpenapi) :*:
        ("discoverModels", ca.discoverModels) :*:
        ("allowedNS", ca.allowedNS) :*:
        ("excludedNS", ca.excludedNS) :*:
        ("repositories", ca.repositories) :*:
        ("dependencies", ca.dependencies) :*:
        ("transformers", ca.transformers) :*:
        ("localJars", ca.localJars) :*:
        LNil
    },
    {
      case (_, specs) :*:
          (_, output) :*:
          (_, openapiOutput) :*:
          (_, skipScala) :*:
          (_, skipOpenapi) :*:
          (_, discoverModels) :*:
          (_, allowedNS) :*:
          (_, excludedNS) :*:
          (_, repositories) :*:
          (_, dependencies) :*:
          (_, transformers) :*:
          (_, localJars) :*: LNil =>
        CodegenArgs(
          specs,
          output,
          openapiOutput,
          skipScala,
          skipOpenapi,
          discoverModels,
          allowedNS,
          excludedNS,
          repositories,
          dependencies,
          transformers,
          localJars
        )
    }
  )

}
