import time
import BaseHTTPServer
import serial
import urlparse
import os, shutil
from voiceid.sr import Voiceid
from voiceid.db import GMMVoiceDB

HOST_NAME = ''
#Porta do servidor
PORT_NUMBER = 9000 
# A configuração: /dev/ttyACM0 é referente a porta USB que o Arduino esta ligado
ser = serial.Serial("/dev/ttyACM0", 9600)
ledLigado = "L".encode()
ledMais = "M".encode()
ledMenos = "m".encode()
fechadura = "F".encode()
tomada1 = "T".encode()
tomada2 = "t".encode()

#Configuracoes do banco de dados da API de voz
caminhoDB = '/home/smarthome/Banco/'
caminhoVoz = '/var/www/html/Voices/'
db = GMMVoiceDB(caminhoDB)

class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
	def do_HEAD(s):
		s.send_response(200)
		s.send_header("Content-type", "text/html")
		s.end_headers()
	def do_GET(s):
		s.send_response(200)
		s.send_header("Content-type", "text/html")
		s.end_headers()				
		path = s.path
		if '-' in path:
			acao, nome, pessoa = path.split('-')
			if acao.replace('/', '') == 'gravar':
				#Adicionando pessoa ao banco
				db.add_model((caminhoVoz + nome), pessoa)
				v = Voiceid(db, (caminhoVoz + nome + ".wav"))
				s.wfile.write("Gravado")
			if acao.replace('/', '') == 'biometria':
				#Verificação de arquivo de voz
				v = Voiceid(db, (caminhoVoz + nome + ".wav"))
				usuario = v.extract_speakers()
				s.wfile.write("%s" % usuario)
				if not "S0 (unknown)" in str(usuario):
					ser.write(ledLigado)
		

		for the_file in os.listdir(caminhoVoz):
		    file_path = os.path.join(caminhoVoz, the_file)
		    try:
			if os.path.isfile(file_path):
			    os.unlink(file_path)
			elif os.path.isdir(file_path): shutil.rmtree(file_path)
		    except Exception, e:
			print e
		if path == '/ledLigado':		
			ser.write(ledLigado)
			s.wfile.write("LED Alterado")
		if path == '/ledMais':		
			ser.write(ledMais)
			s.wfile.write("LED Mais")
		if path == '/ledMenos':		
			ser.write(ledMenos)
			s.wfile.write("LED Menos")
		if path == '/fechadura':		
			ser.write(fechadura)
			s.wfile.write("Fechadura aberta")
		if path == '/tomada1':		
			ser.write(tomada1)
			s.wfile.write("Tomada 1 Alterado")
		if path == '/tomada2':		
			ser.write(tomada2)
			s.wfile.write("Tomada 2 Alterado")
if __name__ == '__main__':
	server_class = BaseHTTPServer.HTTPServer
	httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
	print("Servidor Python Iniciado")
	try:
		httpd.serve_forever()
	except KeyboardInterrupt:
		pass
	httpd.server_close()
	print("Servidor Python parado")
