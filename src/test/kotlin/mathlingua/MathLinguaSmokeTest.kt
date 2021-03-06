/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mathlingua.mathlingua

import assertk.assertThat
import assertk.assertions.isEqualTo
import mathlingua.frontend.FrontEnd
import mathlingua.frontend.support.ValidationFailure
import mathlingua.frontend.support.ValidationSuccess
import mathlingua.newDiskFileSystem
import org.junit.jupiter.api.Test

internal class MathLinguaDataTest {
    @Test
    fun `input MathLingua input file is valid`() {
        val fs = newDiskFileSystem()
        val mathlinguaSourceFile = fs.getFile(listOf("src", "test", "resources", "mathlingua.math"))
        val input = mathlinguaSourceFile.readText()
        val result = FrontEnd.parse(input)
        val builder = StringBuilder()
        if (result is ValidationFailure) {
            for (err in result.errors) {
                builder.append("ERROR: (${err.row + 1}, ${err.column + 1}) ${err.message}\n")
            }
        }
        // The test should fail if there any errors.
        // This assertThat() is used so that the parse
        // errors are printed to the console.
        assertThat(builder.toString()).isEqualTo("")
        assert(result is ValidationSuccess)
    }
}
