/**
 
 É concedida permissão a qualquer pessoa que obtenha uma cópia do código fonte, sendo que o código fonte fornecido não tem qualquer garantia expressa ou implícita, em nenhum caso autores deste código, ou titulares dos diretos autorais são responsáveis por qualquer reivindicação, danos, ou quaisquer responsabilidades decorrente de conexão ou com o uso deste código fonte em qualquer segmento, negócios ou outros softwares
 
 */

package br.org.cip.howto_srcc_api_rest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.util.UUID;
import java.util.function.Consumer;

import com.nimbusds.jose.JOSEException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class App {
	//static final String HOST = "https://apihext.cipsrcc.org.br";
	static final String HOST = "http://localhost:3001";
	
	public static void main(String[] args) throws JOSEException, IOException, CertificateException, ParseException, NoSuchAlgorithmException {
		OkHttpClient client = Utils.getUnsafeOkHttpClient();
		
		{ // Chamada GET
			String requestBody = "";
			String identificadorRequisicao = UUID.randomUUID().toString();

			//Assina requisição
			String xJwsSignature = Utils.signRequest(requestBody, identificadorRequisicao);
			
			//Prepara requisição
			Request request = new Request.Builder()
					.url(HOST + "/api/v1/ferramentas/eco?msg=teste")
					.addHeader("x-jws-signature", xJwsSignature)
					.build();
			
			doCAll(client, request,
					(msgOk) -> System.out.println("Http Get Request - Sucesso: " + msgOk ),
					(msgError) -> System.out.println("Http Get Request - Erro: " + msgError));
		}
		
		{ // Chamada POST
			String jsonBody = 
					"{"
					+ "  \"numCPF\": \"12345678901\","
					+ "  \"tpEmpregador\": 1,"
					+ "  \"numBeneficio\": \"1234567890\","
					+ "  \"numMatricula\": \"12345678901234567890\","
					+ "  \"numContrato\": \"12345678901234567890123456789012345678901234567890\","
					+ "  \"sitRegistro\": \"A\","
					+ "  \"tpEvento\": 1,"
					+ "  \"dtEvento\": \"2020-10-22\","
					+ "  \"dtContratoOrigem\": \"2020-10-22\""
					+ "}";
			
			RequestBody requestBody = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));
			String identificadorRequisicao = "1234124123412341234";

			//Assina requisição
			String xJwsSignature = Utils.signRequest(jsonBody, identificadorRequisicao);
			
			//Prepara requisição
			Request request = new Request.Builder()
					.url(HOST + "/api/v1/RCC0001")
					.addHeader("x-jws-signature", xJwsSignature)
					.post(requestBody)
					.build();
			
			doCAll(client, request,
					(msgOk) -> System.out.println("Http Post Request - Sucesso: " + msgOk ),
					(msgError) -> System.out.println("Http Post Request - Erro: " + msgError));
		}
	}

	private static void doCAll(OkHttpClient client, Request request, Consumer<String> ok, Consumer<String> error)
			throws IOException, CertificateException, ParseException {
		try (Response response = client.newCall(request).execute()) {
			final String body = response.body().string();
			if(response.isSuccessful()) {
				//É necessário sempre verificar a assinatura da resposta
				if(Utils.isSignatureValid(response.header("x-jws-signature"), body)) {
					ok.accept(body);
				} else {
					error.accept("Assinatura inválida. Não foi a CIP que assinou essa requisição");
				}
			} else {
				error.accept(body);
			}
		}
	}
}
