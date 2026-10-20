package chat;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * PubSubChat — Variante 1 (fanout): todos recebem todas as mensagens.
 *
 * Uso: java -cp PubSubChat-1.0-jar-with-dependencies.jar chat.ChatV1 amqp://cd:<pass>@<IP-VM>:5672
 */
public class ChatV2 {

    public static void main(String[] args) throws Exception {
        String uri = args.length > 0 ? args[0] : "amqp://guest:guest@localhost:5672";
        Scanner in = new Scanner(System.in);
        System.out.print("Nome: ");
        String nome = in.nextLine();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(uri);
        try (Connection conn = factory.newConnection(); Channel ch = conn.createChannel()) {

            // TODO 1: declarar o exchange
            ch.exchangeDeclare("chat.temas", BuiltinExchangeType.DIRECT, true);

            // Filas não temporárias, cada uma com o seu tema dedicado
            Map<String, String> filas = new HashMap<>();
            filas.put(
                "desporto",
                ch.queueDeclare("desporto", true, false, false, null)
                  .getQueue()
            );
            filas.put(
                "tecnologia",
                ch.queueDeclare("tecnologia", true, false, false, null)
                  .getQueue()
            );
            filas.put(
                "cinema",
                ch.queueDeclare("cinema", true, false, false, null)
                  .getQueue()
            );
            filas.put(
                "economia",
                ch.queueDeclare("economia", true, false, false, null)
                  .getQueue()
            );

            // TODO 2: criar o(s) binding(s) da fila ao exchange
            filas.forEach((tema, fila) -> {
                try {
                    ch.queueBind(fila, "chat.temas", tema);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

            System.out.print("Temas (separar apenas por vírgula): ");
            String[] temas = in.nextLine().split(",");

            for (String tema : temas) {
                ch.basicConsume(filas.get(tema), true,  (tag, msg) -> {
                    String texto = new String(msg.getBody(), StandardCharsets.UTF_8);
                    System.out.printf("%s: %s\n", msg.getEnvelope().getRoutingKey(), texto);
                }, tag -> {});
            }

            System.out.println("Escreva mensagens (/sair para terminar)");
            while (true) {
                String linha = in.nextLine();
                if (linha.equals("/sair")) break;
                String[] particoes = linha.split(": ", 2);
                if (particoes.length != 2) {
                    System.out.println(
                        "Por favor indique a sua mensagem no formato" +
                        "\"<tema (no caso de múltiplos, separar apenas por vírgula)>: <mensagem>\""
                    );
                    continue;
                }

                for (String fila: particoes[0].split(",")) {
                    // TODO 4: publicar a mensagem, no formato "tema: texto"
                    ch.basicPublish(
                        "chat.temas",
                        fila,
                        null,
                        particoes[1].getBytes(StandardCharsets.UTF_8)
                    );
                }


            }
        }
    }
}
