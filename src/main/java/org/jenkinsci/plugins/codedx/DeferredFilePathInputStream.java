/*
 * © 2023 Synopsys, Inc. All rights reserved worldwide.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package org.jenkinsci.plugins.codedx;

import hudson.FilePath;

import java.io.IOException;
import java.io.InputStream;

public class DeferredFilePathInputStream extends InputStream {
	FilePath fp;
	InputStream is;

	public DeferredFilePathInputStream(FilePath fp)
	{
		this.fp = fp;
		this.is = null;
	}

	private void initStream() throws IOException {
		if (this.is == null) {
			try {
				this.is = fp.read();
			} catch (InterruptedException e) {
				throw new IOException("Operation was interrupted", e);
			}
		}
	}

	@Override
	public void close() throws IOException {
		if (this.is != null) {
			this.is.close();
			this.is = null;
		}
	}

	@Override
	public int read(byte[] b) throws IOException {
		initStream();

		int numRead = this.is.read(b);
		if (numRead < 0) {
			close();
		}

		return numRead;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		initStream();

		int numRead = this.is.read(b, off, len);
		if (numRead < 0) {
			close();
		}

		return numRead;
	}

	@Override
	public int read() throws IOException {
		initStream();

		int result = this.is.read();
		if (result < 0) {
			this.is.close();
		}

		return result;
	}
}
