package au.com.permeance.utility.scriptinghelper.socket;

import java.io.OutputStream;

/**
 * @author marium
 */
public class WebSocketOutputStream extends OutputStream {

	public WebSocketOutputStream(long backgroundTaskId) {
		this.backgroundTaskId = backgroundTaskId;
	}

	@Override
	public void flush() {
		flushBuffer(); // Assurez-vous d'appeler cette méthode après chaque écriture
	}

	@Override
	public void write(int b) {
		buffer.append((char)b);

		// Par exemple, envoyer le message après un retour à la ligne

		if ((char)b == '\n') {
			flushBuffer();
		}
	}

	private void flushBuffer() {
		if (buffer.length() > 0) {
			BackgroundTasksSocket.addToOutputMap(
				backgroundTaskId, buffer.toString());
			buffer.setLength(0); // Réinitialise le buffer
		}
	}

	private long backgroundTaskId;
	private StringBuilder buffer = new StringBuilder();

}