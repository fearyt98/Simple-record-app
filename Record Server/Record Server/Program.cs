using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Timers;
using System.Drawing;
using System.Drawing.Imaging;

namespace Record_Server {
    class Server {
        public bool running = false; //статус сервера

        private int timeout = 8; //лимит времени на приём данных
        private Encoding charEncoder = Encoding.UTF8; //кодировка
        private Socket serverSocket; //сокет
        private string contentPath; //папка для контента

        private Dictionary<string, string> extensions = new Dictionary<string, string>() {
            { "png", "image/png" },
            { "gif", "image/gif" },
            { "jpg", "image/jpg" },
            { "jpeg", "image/jpeg" }
        };
        public bool Start(IPAddress ipAddress, int port, int maxCon, string contentPath) {
            if (running) return false;

            try {
                serverSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
                serverSocket.Bind(new IPEndPoint(ipAddress, port));
                serverSocket.Listen(maxCon); //максимальная число подключений (слушателей)
                serverSocket.ReceiveTimeout = timeout;
                serverSocket.SendTimeout = timeout;
                running = true;
                this.contentPath = contentPath;
            } catch { return false; }

            Thread requestListenerThread = new Thread(() => {
                while (running) {
                    Socket clientSocket;
                    try {
                        clientSocket = serverSocket.Accept();
                        Thread requestHandler = new Thread(() => {
                            clientSocket.ReceiveTimeout = timeout;
                            clientSocket.SendTimeout = timeout;
                            try {
                                handleRequest(clientSocket);
                            } catch {
                                try {
                                    clientSocket.Close();
                                } catch { }
                            }
                        });
                        requestHandler.Start();
                    } catch { }
                }
            });
            requestListenerThread.Start();
            return true;
        }
        public void Stop() {
            if (running) {
                running = false;
                try { serverSocket.Close(); } catch { }
                serverSocket = null;
            }
        }
        private void handleRequest(Socket clientSocket) {
            byte[] buffer = new byte[10240];
            int receivedBCount = clientSocket.Receive(buffer);
            string strReceived = charEncoder.GetString(buffer, 0, receivedBCount);

            string httpMethod = strReceived.Substring(0, strReceived.IndexOf(" "));
            int start = strReceived.IndexOf(httpMethod) + httpMethod.Length + 1;
            int length = strReceived.LastIndexOf("HTTP") - start - 1;
            string requestUrl = strReceived.Substring(start, length);
            string requestFile;
            if (httpMethod.Equals("GET") || httpMethod.Equals("POST"))
                requestFile = requestUrl.Split('?')[0];
            else {
                notImplemented(clientSocket);
                return;
            }
            requestFile = requestFile.Replace("/", "\\").Replace("\\..", "");
            start = requestFile.LastIndexOf('.') + 1;
            if (start > 0) {
                length = requestFile.Length - start;
                string extension = requestFile.Substring(start, length);
                if (extensions.ContainsKey(extension)) {
                    if (File.Exists(contentPath + requestFile))
                        sendOkResponse(clientSocket, File.ReadAllBytes(contentPath + requestFile), extensions[extension]);
                    else notFound(clientSocket);
                }
            } else {
                // Если файл не указан
                notFound(clientSocket);
            }
        }
        private void notImplemented(Socket clientSocket) {
            sendResponse(clientSocket,
                "Метод не реализован",
                "501 Not Implemented",
                "text/html");
        }

        private void notFound(Socket clientSocket) {
            sendResponse(clientSocket,
                "Не найдено",
                "404 Not Found",
                "text/html");
        }
        private void sendOkResponse(Socket clientSocket, byte[] bContent, string contentType) {
            sendResponse(clientSocket, bContent, "200 OK", contentType);
        }

        private void sendResponse(Socket clientSocket, string strContent, string responseCode, string contentType) {
            byte[] bContent = charEncoder.GetBytes(strContent);
            sendResponse(clientSocket, bContent, responseCode, contentType);
        }

        private void sendResponse(Socket clientSocket, byte[] bContent, string responseCode,
                          string contentType) {
            try {
                byte[] bHeader = charEncoder.GetBytes(
                         "HTTP/1.1 " + responseCode + "\r\n"
                          + "Content-Length: " + bContent.Length.ToString() + "\r\n"
                          + "Connection: close\r\n"
                          + "Content-Type: " + contentType + "\r\n\r\n"
                    );
                clientSocket.Send(bHeader);
                clientSocket.Send(bContent);
                clientSocket.Close();
            } catch { }
        }
    }
    class Program {
        static string contentPath = @"E:\Record Server\Content\";
        static IPAddress ipAddress = IPAddress.Parse("192.168.43.153");
        static int port = 4000;
        static int maxСonn = 60;

        static void Main(string[] args) {
            System.Timers.Timer timer = new System.Timers.Timer(500); //2 кадра секунду
            timer.Elapsed += OnTimedEvent;
            timer.AutoReset = true;

            Server server = new Server();
            server.Start(ipAddress, port, maxСonn, contentPath);
            timer.Start();
            Console.WriteLine("Sever running");
            Console.WriteLine("Press any key for stop srever");
            if (Console.ReadKey() != null) {
                timer.Stop();
                server.Stop();
            }
        }
        private static void OnTimedEvent(Object source, ElapsedEventArgs e) {
            Bitmap bmp = new Bitmap(1920, 1080);
            Graphics gr = Graphics.FromImage(bmp);
            gr.CopyFromScreen(0, 0, 0, 0, new Size(bmp.Width, bmp.Height));
            bmp.Save(contentPath + "screenshot.jpg", ImageFormat.Jpeg);  // сохранение изображения      
            bmp.Dispose();
            gr.Dispose();
        }
    }
}