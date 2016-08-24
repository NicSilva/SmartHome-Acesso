//Pinos usados

int LED_PIN = 6;
int FECHADURA_PIN = 3;
int TOM1_PIN = 4;
int TOM2_PIN = 5;
int INT = 250;
char val;

void setup()
{
  Serial.begin(9600); //Porta da serial
  
  //Indicando que serao penos de saida
  pinMode(FECHADURA_PIN, OUTPUT);
  pinMode(TOM1_PIN, OUTPUT);
  pinMode(TOM2_PIN, OUTPUT);
}

void loop() {  
  if(Serial.available() > 0) //Verifica se a serial esta ligada
   {
     val = Serial.read();
     if(val != 'NULL') {  //Se a entrada serial for diferente de nulo
         if(val == 'T') //Caso a entrada seja igual a L em dicimal
         {
           if(digitalRead(TOM1_PIN) == LOW){ //Verifica se esta desligado, caso sim, ele liga, caso no, desliga
             digitalWrite(TOM1_PIN, HIGH);
             Serial.println("Tomada 1 Ligada");  
           } else {
             digitalWrite(TOM1_PIN, LOW); 
             Serial.println("Tomada 1 Desligada"); //Posta uma mensagem na serial
           }
         } 
         else if(val == 't') //Caso a entrada seja igual a P em dicimal
         {
           if(digitalRead(TOM2_PIN) == LOW){
             digitalWrite(TOM2_PIN, HIGH); 
             Serial.println("Tomada 2 Ligada");
           } else {
             digitalWrite(TOM2_PIN, LOW); 
             Serial.println("Tomada 2 Desligada");
           }  
         } 
         else if(val == 'F') //Caso a entrada seja igual a L em dicimal
         {
           if(digitalRead(FECHADURA_PIN) == LOW) { //Verifica se esta desligado, caso sim, ele liga, caso no, desliga
             digitalWrite(FECHADURA_PIN, HIGH);
             delay(500);
             digitalWrite(FECHADURA_PIN, LOW);
             Serial.println("Fechadura aberta");  
           }
         } 
         else if(val == 'L') //Caso a entrada seja igual a L em dicimal
         {
           if (INT != 250) {
             INT = 250;
             analogWrite(LED_PIN, INT);  
             Serial.println("Luz ligada");  
           } else {
             INT = 0;
             analogWrite(LED_PIN, INT);  
             Serial.println("Luz desligada"); 
           }
         } 
         else if(val == 'M') //Caso a entrada seja igual a L em dicimal
         {
           if (INT < 250){
             INT += 25;
           }
           analogWrite(LED_PIN, INT);  
           Serial.println("Mais luz");  
         } 
         else if(val == 'm') //Caso a entrada seja igual a L em dicimal
         {
           if (INT > 25){
             INT -= 25;
           }
           analogWrite(LED_PIN, INT);  
           Serial.println("Menos luz");  
         } 
       }
    }
}

