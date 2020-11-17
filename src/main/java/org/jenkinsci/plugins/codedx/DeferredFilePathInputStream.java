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
