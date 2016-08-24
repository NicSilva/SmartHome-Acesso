package com.example.smarthome_v2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ExtAudioRecorder extAudioRecorder;
	private String arquivo;
	private static ImageView btnOnOff;
	private Button btnLed;
	private Button btnMais;
	private Button btnMenos;
	private Button btnTomada1;
	private Button btnTomada2;
	private Button btnPorta;
	private static ScrollView adminPanel;
	private static EditText txtIpServidor;
	private static String ip;
	private boolean EmGravacao = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.authentication_screen);

		btnOnOff = (ImageView) findViewById(R.id.btnOnOff);
		btnLed = (Button) findViewById(R.id.btnLED);
		btnMais = (Button) findViewById(R.id.btnMais);
		btnMenos = (Button) findViewById(R.id.btnMenos);
		btnTomada1 = (Button) findViewById(R.id.btnTomada1);
		btnTomada2 = (Button) findViewById(R.id.btnTomada2);
		btnPorta = (Button) findViewById(R.id.btnPorta);
		txtIpServidor = (EditText) findViewById(R.id.txtIP);
		adminPanel = (ScrollView) findViewById(R.id.adminPanel);

		ip = txtIpServidor.getText().toString();
		
		btnMais.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new sendCommand(getApplicationContext(), "ledMais").execute();
			}
		});
		
		btnMenos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new sendCommand(getApplicationContext(), "ledMenos").execute();
			}
		});
		
		btnTomada1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new sendCommand(getApplicationContext(), "tomada1").execute();
			}
		});
		
		btnTomada2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new sendCommand(getApplicationContext(), "tomada2").execute();
			}
		});
		
		btnPorta.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new sendCommand(getApplicationContext(), "fechadura").execute();
			}
		});
		
		btnLed.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				new sendCommand(getApplicationContext(), "ledLigado").execute();
			}
		});

		arquivo = Environment.getExternalStorageDirectory() + "/TempSmartHome.wav";

		btnOnOff.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (!EmGravacao) {
					File sourceFile = new File(arquivo);
					if (sourceFile.isFile())
						sourceFile.delete();

					EmGravacao = true;
					btnOnOff.setImageResource(R.drawable.mic_on);
					extAudioRecorder = ExtAudioRecorder.getInstanse(false);
					extAudioRecorder.setOutputFile(arquivo);
					extAudioRecorder.prepare();
					extAudioRecorder.start();
					Toast.makeText(getApplicationContext(), "Fale para verificação de voz", Toast.LENGTH_SHORT).show();
				} else {
					EmGravacao = false;
					btnOnOff.setImageResource(R.drawable.mic_off);
					extAudioRecorder.stop();
					extAudioRecorder.release();
					Toast.makeText(getApplicationContext(), "Enviando voz", Toast.LENGTH_SHORT).show();
					new UploadFile(arquivo, getApplicationContext()).execute();
				}
			}
		});
	}

	// TODO -- UploadFile Class --
	public static class UploadFile extends AsyncTask<Void, String, String> {

		private String path;
		private Context apli;

		public UploadFile(String path, Context apli) {
			this.path = path;
			this.apli = apli;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(Void... v) {
			String retorno = "";
			try {
				retorno = uploadFile(path, apli);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return retorno;
		}

		@Override
		protected void onPostExecute(String result) {
			if(result.equals("success")) {
				Toast.makeText(apli, "Verificando", Toast.LENGTH_SHORT).show();
				new sendCommand(apli, "biometria-TempSmartHome-verificar").execute();
			} else {
				Toast.makeText(apli, "Erro:\n" + result, Toast.LENGTH_SHORT).show();
			}
		}
	}

	// TODO -- UploadFile Class --
	public static class sendCommand extends AsyncTask<Void, String, String> {

		private Context apli;
		private String entrada;

		public sendCommand(Context apli, String entrada) {
			this.apli = apli;
			this.entrada = entrada;
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected String doInBackground(Void... v) {
			String retorno = "";
			try {
				retorno = postData(entrada);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return retorno;
		}

		@Override
		protected void onPostExecute(String result) {
			if(entrada.equals("biometria-TempSmartHome-verificar")) {
				if(!result.contains("S0 (unknown)") && !result.equals("")) {
					Log.e("Nome Usuario", result);
					adminPanel.setVisibility(View.VISIBLE);
					txtIpServidor.setEnabled(false);
					btnOnOff.setVisibility(View.GONE);
					String nome = result.replace("S0 (", "").replace(")", "");
					Toast.makeText(apli, "Usuário: " + nome + "\nAcesso liberado.", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(apli, "Usuário não identificado", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(apli, result, Toast.LENGTH_SHORT).show();
			}
		}
	}

	public static String streamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

	public static String postData(String entrada) throws JSONException {
		// Create a new HttpClient and Post Header
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httppost = new HttpGet("http://"+ ip + ":9000/" + entrada);
		JSONObject json = new JSONObject();
		String text = "";

		try {
			HttpResponse response = httpclient.execute(httppost);

			if (response != null) {
				InputStream is = response.getEntity().getContent();

				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
				StringBuilder sb = new StringBuilder();

				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				text = sb.toString();
			}

		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}

		return text;
	}

	// TODO -- UploadFile --
	public static String uploadFile(String sourceFileUri, Context apli) throws IOException {

		String fileName = sourceFileUri;

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";
		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1 * 1024 * 1024;
		File sourceFile = new File(sourceFileUri);
		String upLoadServerUri = "http://"+ ip + "/uploadFile.php";

		if (sourceFile.isFile()) {
			int serverResponseCode = 0;
			String serverResponseMessage = "";
			InputStream is = null;
			try {
				// open a URL connection to the Servlet
				FileInputStream fileInputStream = new FileInputStream(sourceFile);
				URL url = new URL(upLoadServerUri);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();
				conn.setDoInput(true); // Allow Inputs
				conn.setDoOutput(true); // Allow Outputs
				conn.setUseCaches(false); // Don't use a Cached Copy
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				conn.setRequestProperty("ENCTYPE", "multipart/form-data");
				conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				conn.setRequestProperty("uploaded_file", fileName);

				dos = new DataOutputStream(conn.getOutputStream());

				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + fileName + "\""
						+ lineEnd);

				dos.writeBytes(lineEnd);

				// create a buffer of maximum size
				bytesAvailable = fileInputStream.available();

				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				buffer = new byte[bufferSize];

				// read file and write it into form...
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);

				Log.e("uploadFile", "Tentando mandar");

				while (bytesRead > 0) {
					dos.write(buffer, 0, bufferSize);
					bytesAvailable = fileInputStream.available();
					bufferSize = Math.min(bytesAvailable, maxBufferSize);
					bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				}

				// send multipart form data necesssary after file data...
				dos.writeBytes(lineEnd);
				dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

				// Responses from the server (code and message)
				serverResponseCode = conn.getResponseCode();
				serverResponseMessage = conn.getResponseMessage();

				if (serverResponseCode >= 400) {
					is = conn.getErrorStream();

				} else {
					is = conn.getInputStream();
				}

				Log.e("uploadFile", "HTTP Response is : " + serverResponseMessage + ": " + serverResponseCode);

				if (serverResponseCode == 200) {
					// Evento para caso ocorra tudo bem no carregamento da
					// pagina
					// if (sourceFile.isFile())
					// sourceFile.delete();
				} else {
					// if (sourceFile.isFile())
					// sourceFile.delete();
				}

				// close the streams //
				fileInputStream.close();
				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {
				ex.printStackTrace();
				Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Upload file to server Exception", "Exception : " + e.getMessage(), e);
			}

			return streamToString(is);
		}
		return "Nada";
	}
}
