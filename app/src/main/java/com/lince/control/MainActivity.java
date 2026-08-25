package com.lince.control;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.appcompat.app.AlertDialog;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Executor;

public class MainActivity extends androidx.appcompat.app.AppCompatActivity {
	
	private WebView myWebView;
	private LinearLayout logoSplash;
	private AndroidAppInterface sharingBridge;
	
	private String correoLoginPendiente = "";
	private static final String PREFS_NAME = "SesionLince";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		myWebView = findViewById(R.id.webView);
		logoSplash = findViewById(R.id.logoSplash);
		
		sharingBridge = new AndroidAppInterface();
		
		myWebView.setBackgroundColor(0xFFFFFFFF);
		
		WebSettings webSettings = myWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		
		webSettings.setDomStorageEnabled(true);
		webSettings.setDatabaseEnabled(true);
		
		String cacheDirPath = getCacheDir().getAbsolutePath();
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.setAcceptThirdPartyCookies(myWebView, true);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cookieManager.flush();
		} else {
			CookieSyncManager.createInstance(this);
			CookieSyncManager.getInstance().sync();
		}
		
		webSettings.setSupportMultipleWindows(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		
		myWebView.setWebChromeClient(new WebChromeClient() {
			private View mCustomView;
			private WebChromeClient.CustomViewCallback mCustomViewCallback;
			
			@Override
			public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
				if (isUserGesture) {
					Dialog popupDialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
					
					LinearLayout mainLayout = new LinearLayout(MainActivity.this);
					mainLayout.setOrientation(LinearLayout.VERTICAL);
					mainLayout.setBackgroundColor(0xFFFFFFFF);
					
					RelativeLayout actionBar = new RelativeLayout(MainActivity.this);
					actionBar.setBackgroundColor(0xFF000000);
					
					float scale = getResources().getDisplayMetrics().density;
					int heightPx = (int) (56 * scale + 0.5f);
					int paddingPx = (int) (16 * scale + 0.5f);
					
					actionBar.setLayoutParams(new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
					
					TextView btnCerrar = new TextView(MainActivity.this);
					btnCerrar.setId(View.generateViewId());
					btnCerrar.setText("✕");
					btnCerrar.setTextSize(20);
					btnCerrar.setTextColor(0xFFFFFFFF);
					btnCerrar.setPadding(paddingPx, 0, paddingPx, 0);
					btnCerrar.setGravity(Gravity.CENTER_VERTICAL);
					
					RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
					btnParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					btnCerrar.setLayoutParams(btnParams);
					actionBar.addView(btnCerrar);
					
					TextView txtTitulo = new TextView(MainActivity.this);
					txtTitulo.setText("Enlace Externo");
					txtTitulo.setTextSize(16);
					txtTitulo.setTextColor(0xFFFFFFFF);
					txtTitulo.setTypeface(android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.NORMAL));
					
					txtTitulo.setSingleLine(true);
					txtTitulo.setEllipsize(android.text.TextUtils.TruncateAt.END);
					txtTitulo.setGravity(Gravity.CENTER);
					
					RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					titleParams.addRule(RelativeLayout.CENTER_IN_PARENT);
					titleParams.addRule(RelativeLayout.RIGHT_OF, btnCerrar.getId());
					int marginPx = (int) (16 * scale + 0.5f);
					titleParams.setMargins(0, 0, marginPx, 0);
					
					txtTitulo.setLayoutParams(titleParams);
					actionBar.addView(txtTitulo);
					
					View divisor = new View(MainActivity.this);
					divisor.setBackgroundColor(0xFF222222);
					LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, (int) (1 * scale + 0.5f));
					
					mainLayout.addView(actionBar);
					mainLayout.addView(divisor, divParams);
					
					WebView popupWebView = new WebView(MainActivity.this);
					popupWebView.getSettings().setJavaScriptEnabled(true);
					popupWebView.getSettings().setDomStorageEnabled(true);
					popupWebView.getSettings().setBuiltInZoomControls(true);
					popupWebView.getSettings().setDisplayZoomControls(false);
					
					popupWebView.setWebViewClient(new WebViewClient() {
						@Override
						public void onPageFinished(WebView view, String url) {
							super.onPageFinished(view, url);
							if (view.getTitle() != null && !view.getTitle().isEmpty()) {
								txtTitulo.setText(view.getTitle());
							}
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
								CookieManager.getInstance().flush();
							} else {
								CookieSyncManager.getInstance().sync();
							}
						}
					});
					
					mainLayout.addView(popupWebView, new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
					
					btnCerrar.setOnClickListener(v -> popupDialog.dismiss());
					
					WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
					transport.setWebView(popupWebView);
					resultMsg.sendToTarget();
					
					popupDialog.setContentView(mainLayout);
					popupDialog.show();
					return true;
				}
				
				WebView printWebView = new WebView(MainActivity.this);
				printWebView.getSettings().setJavaScriptEnabled(true);
				
				printWebView.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						super.onPageFinished(view, url);
						android.print.PrintManager printManager = (android.print.PrintManager) getSystemService(Context.PRINT_SERVICE);
						if (printManager != null) {
							String jobName = "Reporte_El_Lince_" + System.currentTimeMillis();
							printManager.print(jobName, view.createPrintDocumentAdapter(jobName), null);
						}
					}
				});
				
				WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
				transport.setWebView(printWebView);
				resultMsg.sendToTarget();
				return true;
			}
			
			@Override
			public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
				super.onShowCustomView(view, callback);
				if (mCustomView != null) {
					callback.onCustomViewHidden();
					return;
				}
				mCustomView = view;
				mCustomViewCallback = callback;
				
				logoSplash.setVisibility(View.GONE);
				myWebView.setVisibility(View.GONE);
				
				getWindow().getDecorView().findViewById(android.R.id.content).setBackgroundColor(0xFFFFFFFF);
				((android.view.ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content)).addView(mCustomView);
			}
			
			@Override
			public void onHideCustomView() {
				super.onHideCustomView();
				if (mCustomView == null) return;
				
				((android.view.ViewGroup) getWindow().getDecorView().findViewById(android.R.id.content)).removeView(mCustomView);
				mCustomView = null;
				mCustomViewCallback.onCustomViewHidden();
				
				myWebView.setVisibility(View.VISIBLE);
			}
		});
		
		myWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				runOnUiThread(() -> {
					logoSplash.animate().cancel();
					myWebView.animate().cancel();
					if (logoSplash.getVisibility() != View.VISIBLE) {
						logoSplash.setAlpha(1f);
						logoSplash.setVisibility(View.VISIBLE);
					}
					myWebView.animate().alpha(0f).setDuration(150).withEndAction(() -> {
						if (logoSplash.getVisibility() == View.VISIBLE) {
							myWebView.setVisibility(View.GONE);
						}
					}).start();
				});
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					CookieManager.getInstance().flush();
				} else {
					CookieSyncManager.getInstance().sync();
				}
				
				String javascriptCSS = "var style = document.createElement('style');" +
				"style.innerHTML = '.leaflet-control-fullscreen-button { background-image: none !important; display: flex !important; align-items: center !important; justify-content: center !important; font-size: 22px !important; color: #333333 !important; font-weight: bold !important; text-decoration: none !important; } .leaflet-control-fullscreen-button::before { content: \"⛶\" !important; }';" +
				"document.head.appendChild(style);";
				view.evaluateJavascript(javascriptCSS, null);
				
				String scriptModificador = "if (typeof $ !== 'undefined' && $.fn.dataTable) {" +
				"    $('.buttons-excel').off('click').on('click', function(e) {" +
				"        e.preventDefault(); e.stopImmediatePropagation();" +
				"        var dt = $('.table').DataTable();" +
				"        var config = dt.button('.buttons-excel').config();" +
				"        config.createObjectURL = false;" +
				"        var xlsx = $.fn.dataTable.ext.buttons.excelHtml5._createXlsx(config, dt);" +
				"        xlsx.generateAsync({ type: 'base64' }).then(function(base64) {" +
				"            if(typeof Android !== 'undefined') Android.sharePdf(base64, 'Reporte_' + Date.now() + '.xlsx');" +
				"        });" +
				"    });" +
				"    $('.buttons-pdf').off('click').on('click', function(e) {" +
				"        e.preventDefault(); e.stopImmediatePropagation();" +
				"        var dt = $('.table').DataTable();" +
				"        var config = dt.button('.buttons-pdf').config();" +
				"        var pdfMakeInstance = $.fn.dataTable.ext.buttons.pdfHtml5._createDoc(config, dt);" +
				"        pdfMakeInstance.getBase64(function(base64) {" +
				"            if(typeof Android !== 'undefined') Android.sharePdf(base64, 'Reporte_' + Date.now() + '.pdf');" +
				"        });" +
				"    });" +
				"    $('.buttons-csv').off('click').on('click', function(e) {" +
				"        e.preventDefault(); e.stopImmediatePropagation();" +
				"        var dt = $('.table').DataTable();" +
				"        var config = dt.button('.buttons-csv').config();" +
				"        var csvData = $.fn.dataTable.ext.buttons.csvHtml5.action.call(dt, null, dt, $('.buttons-csv'), config);" +
				"        if(csvData && typeof Android !== 'undefined') {" +
				"            var base64 = btoa(unescape(encodeURIComponent(csvData)));" +
				"            Android.sharePdf(base64, 'Reporte_' + Date.now() + '.csv');" +
				"        }" +
				"    });" +
				"}";
				view.evaluateJavascript(scriptModificador, null);
				
				runOnUiThread(() -> {
					myWebView.animate().cancel();
					logoSplash.animate().cancel();
					myWebView.setAlpha(0f);
					myWebView.setVisibility(View.VISIBLE);
					myWebView.animate().alpha(1f).setDuration(250).start();
					logoSplash.animate().alpha(0f).setDuration(250).withEndAction(() -> logoSplash.setVisibility(View.GONE)).start();
				});
			}
			
			@Override
			public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
				if (errorCode == ERROR_HOST_LOOKUP || errorCode == ERROR_CONNECT || errorCode == ERROR_TIMEOUT || errorCode == ERROR_UNKNOWN) {
					view.loadUrl("file:///android_asset/sin_internet.html");
				}
                        }


                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                if (url.startsWith("geo:") || url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("whatsapp:") || url.startsWith("intent:")) {
                                        try {
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                Intent chooser = Intent.createChooser(intent, "Abrir con");
                                                startActivity(chooser);
                                        } catch (Exception e) {
                                                e.printStackTrace();
                                        }
                                        return true;
                                }
                                return false;
                        }
		});
		
		myWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
			runOnUiThread(() -> {
				try {
					if (url.startsWith("data:") || url.startsWith("blob:")) {
						return;
					}
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(url));
					startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		});
		
		myWebView.addJavascriptInterface(new WebAppInterface(this), "AndroidHuella");
		myWebView.addJavascriptInterface(sharingBridge, "Android");
		
		myWebView.loadUrl("https://control.mudanzasellince.com/?timeout=1");
	}
	
	@Keep
	public class AndroidAppInterface {
		@Keep
		@JavascriptInterface
		public void sharePdf(final String base64Data, final String fileName) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					try {
						if (base64Data == null || base64Data.trim().isEmpty() || base64Data.equals("undefined")) {
							Toast.makeText(MainActivity.this, "Error: Datos de exportación inválidos", Toast.LENGTH_SHORT).show();
							return;
						}
						
						String cleanBase64 = base64Data;
						if (cleanBase64.contains(",")) {
							cleanBase64 = cleanBase64.substring(cleanBase64.indexOf(",") + 1);
						}
						
						byte[] pdfBytes = Base64.decode(cleanBase64.trim(), Base64.DEFAULT);
						File pdfFile = new File(getCacheDir(), fileName);
						
						FileOutputStream stream = new FileOutputStream(pdfFile);
						stream.write(pdfBytes);
						stream.flush();
						stream.close();
						
						Uri contentUri = FileProvider.getUriForFile(MainActivity.this, "com.lince.control.fileprovider", pdfFile);
						
						if (contentUri != null) {
							Intent shareIntent = new Intent();
							shareIntent.setAction(Intent.ACTION_SEND);
							shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
							
							if (fileName.endsWith(".xlsx")) {
								shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
							} else if (fileName.endsWith(".csv")) {
								shareIntent.setType("text/csv");
							} else {
								shareIntent.setType("application/pdf");
							}
							
							shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							startActivity(Intent.createChooser(shareIntent, "Compartir Reporte / Documento"));
						} else {
							Toast.makeText(MainActivity.this, "No se pudo generar la URI del archivo", Toast.LENGTH_SHORT).show();
						}
					} catch (final Exception e) {
						e.printStackTrace();
						Toast.makeText(MainActivity.this, "Error de escritura: " + e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}
	
	@Keep
	public class WebAppInterface {
		MainActivity mActivity;
		
		WebAppInterface(MainActivity activity) {
			mActivity = activity;
		}
		
		@Keep
		@JavascriptInterface
		public void mostrarToast(final String mensaje) {
			mActivity.runOnUiThread(() -> Toast.makeText(mActivity, mensaje, Toast.LENGTH_SHORT).show());
		}
		
		@Keep
		@JavascriptInterface
		public void guardarTokenSesion(final String token, final String correo) {
			if (correo == null || correo.trim().isEmpty()) return;
			String correoClean = correo.trim().toLowerCase();
			SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			prefs.edit().putString("token_" + correoClean, token).apply();
		}
		
		@Keep
		@JavascriptInterface
		public void verificarHuellaLocal(final String correo) {
			if (correo == null || correo.trim().isEmpty()) return;
			final String correoClean = correo.trim().toLowerCase();
			
			SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			boolean yaTieneHuellaLocal = prefs.getBoolean("huella_registrada_" + correoClean, false);
			
			if (!yaTieneHuellaLocal) {
				mActivity.runOnUiThread(() -> {
					new AlertDialog.Builder(mActivity)
					.setTitle("🔑 Activar Inicio Rápido")
					.setMessage("¿Deseas activar el ingreso con tu huella dactilar para tus próximos inicios de sesión?")
					.setCancelable(false)
					.setPositiveButton("Sí, activar", (dialog, which) -> {
						myWebView.evaluateJavascript("javascript:solicitarRegistroHuellaWeb('" + correoClean + "')", null);
					})
					.setNegativeButton("Luego", null)
					.show();
				});
			} else {
				Log.d("HUELLA_DEBUG", "El usuario ya tiene registro biométrico local para: " + correoClean);
			}
		}
		
		@Keep
		@JavascriptInterface
		public void dispararLectorNativoRegistro(final String correo) {
			if (correo == null || correo.trim().isEmpty()) return;
			mActivity.runOnUiThread(() -> {
				correoLoginPendiente = correo.trim().toLowerCase();
				if (tieneInternet()) {
					ejecutarLectorBiometrico();
				} else {
					Toast.makeText(mActivity, "Sin conexión a Internet.", Toast.LENGTH_SHORT).show();
				}
			});
		}
		
		@Keep
		@JavascriptInterface
		public void recibirCorreoRegistro(final String correoUsuario) {
			if (correoUsuario == null || correoUsuario.trim().isEmpty()) return;
			mActivity.runOnUiThread(() -> {
				correoLoginPendiente = correoUsuario.trim().toLowerCase();
				SharedPreferences prefs = mActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
				prefs.edit().putString("correo_registro", correoLoginPendiente).apply();
				
				if (tieneInternet()) {
					ejecutarLectorBiometrico();
				} else {
					Toast.makeText(mActivity, "Sin conexión a Internet. No se puede registrar la huella.", Toast.LENGTH_LONG).show();
				}
			});
		}
		
		@Keep
		@JavascriptInterface
		public void iniciarAutenticacionNativa(final String correoUsuario) {
			activarSensorHuella(correoUsuario);
		}
		
		@Keep
		@JavascriptInterface
		public void activarSensorHuella(final String correoUsuario) {
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (correoUsuario == null || correoUsuario.trim().isEmpty()) {
						Toast.makeText(mActivity, "Por favor, ingresa tu correo electrónico primero.", Toast.LENGTH_LONG).show();
						return;
					}
					
					if (tieneInternet()) {
						correoLoginPendiente = correoUsuario.trim().toLowerCase();
						ejecutarLectorBiometrico();
					} else {
						Toast.makeText(mActivity, "Sin conexión a Internet. No se puede iniciar sesión.", Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}
	
	private void ejecutarLectorBiometrico() {
		Executor executor = ContextCompat.getMainExecutor(this);
		BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
			@Override
			public void onAuthenticationError(int errorCode, CharSequence errString) {
				super.onAuthenticationError(errorCode, errString);
				runOnUiThread(() -> {
					String urlActual = myWebView.getUrl();
					if (urlActual != null && urlActual.contains("panel_control.php")) {
						myWebView.evaluateJavascript("javascript:resultadoRegistroHuella(false, 'CANCELADO')", null);
					} else if (urlActual != null && urlActual.contains("validar_biometrico.php")) {
						myWebView.evaluateJavascript("javascript:resultadoAutenticacionNativa(false, 'CANCELADO')", null);
					} else {
						myWebView.evaluateJavascript("javascript:window.resultadoHuella(false, 'CANCELADO')", null);
					}
				});
			}
			
			@Override
			public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
				super.onAuthenticationSucceeded(result);
				
				runOnUiThread(() -> {
					if (logoSplash != null && logoSplash.getVisibility() != View.VISIBLE) {
						logoSplash.animate().cancel();
						logoSplash.setAlpha(1f);
						logoSplash.setVisibility(View.VISIBLE);
						myWebView.setVisibility(View.GONE);
					}
					
					String urlActual = myWebView.getUrl();
					SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
					String token = prefs.getString("token_" + correoLoginPendiente, "");
					
					if (urlActual != null && urlActual.contains("panel_control.php")) {
						prefs.edit().putBoolean("huella_registrada_" + correoLoginPendiente, true).apply();
						myWebView.evaluateJavascript("javascript:resultadoRegistroHuella(true, 'OK')", null);
						return;
					}
					
					if (urlActual != null && urlActual.contains("validar_biometrico.php")) {
						myWebView.evaluateJavascript("javascript:resultadoAutenticacionNativa(true, 'OK')", null);
						return;
					}
					
					if (token != null && !token.trim().isEmpty()) {
						myWebView.evaluateJavascript("javascript:window.resultadoHuella(true, 'OK')", null);
					} else {
						myWebView.evaluateJavascript("javascript:window.resultadoHuella(false, 'NO_REGISTRADO')", null);
					}
				});
			}
			
			@Override
			public void onAuthenticationFailed() {
				super.onAuthenticationFailed();
			}
		});
		
		BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
		.setTitle("Control El Lince")
		.setSubtitle("Coloque su huella en el sensor")
		.setNegativeButtonText("Cancelar")
		.build();
		
		biometricPrompt.authenticate(promptInfo);
	}
	
	private boolean tieneInternet() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
			return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
			capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
		}
		return false;
	}
	
	@Override
	public void onBackPressed() {
		if (myWebView != null && myWebView.canGoBack()) {
			myWebView.goBack();
		} else {
			super.onBackPressed();
		}
	}
}
