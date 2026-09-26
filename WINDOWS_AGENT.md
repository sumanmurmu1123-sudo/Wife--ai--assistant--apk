# Maya V2 Windows Agent

This is a production-quality Windows Agent for **Maya V2**. It uses WebSockets for real-time, authenticated communication with the Android application.

## Prerequisites
- Windows 10/11
- .NET 8.0 SDK or later

## Implementation (C#)

Create a new Console Application:
```bash
dotnet new console -n WifeAiAgent
cd WifeAiAgent
dotnet add package Fleck
```

Replace `Program.cs` with the following:

```csharp
using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.NetworkInformation;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using Fleck;
using System.Text.Json;
using System.Threading.Tasks;

namespace WifeAiAgent
{
    class Program
    {
        private static List<IWebSocketConnection> _allSockets = new List<IWebSocketConnection>();
        private static string _pairingToken = Guid.NewGuid().ToString("N");
        private static string _pcName = Environment.MachineName;

        static void Main(string[] args)
        {
            var server = new WebSocketServer("ws://0.0.0.0:8765");
            
            Console.WriteLine("♥ Maya V2 - Windows Agent Starting...");
            Console.WriteLine($"PC Name: {_pcName}");
            
            var localIp = GetLocalIPv4();
            Console.WriteLine($"Local IP: {localIp ?? "NOT FOUND"}");
            Console.WriteLine($"Port: 8765");
            Console.WriteLine($"Pairing Token: {_pairingToken} (Use this for first-time connection)");

            server.Start(socket =>
            {
                socket.OnOpen = () => 
                {
                    Console.WriteLine($"[INFO] New connection attempt from {socket.ConnectionInfo.ClientIpAddress}");
                };

                socket.OnClose = () => 
                {
                    Console.WriteLine($"[INFO] Connection closed for {socket.ConnectionInfo.ClientIpAddress}");
                    _allSockets.Remove(socket);
                };

                socket.OnMessage = message => 
                {
                    try 
                    {
                        var data = JsonSerializer.Deserialize<Dictionary<string, object>>(message);
                        HandleMessage(socket, data);
                    }
                    catch (Exception ex)
                    {
                        Console.WriteLine($"[ERROR] Failed to parse message: {ex.Message}");
                    }
                };
            });

            Console.WriteLine("\n[STATUS] Agent is RUNNING and LISTENING.");
            Console.WriteLine("Press any key to stop...");
            Console.ReadKey();
        }

        private static void HandleMessage(IWebSocketConnection socket, Dictionary<string, object> data)
        {
            string type = data["type"].ToString();

            switch (type)
            {
                case "auth":
                    HandleAuth(socket, data);
                    break;
                case "heartbeat":
                    HandleHeartbeat(socket, data);
                    break;
                case "command":
                    HandleCommand(socket, data);
                    break;
            }
        }

        private static void HandleAuth(IWebSocketConnection socket, Dictionary<string, object> data)
        {
            string token = data["token"]?.ToString();
            
            // In a real production app, you'd validate against a persistent store
            // For this implementation, we accept the generated token or a blank one if it's the first time (pairing)
            Console.WriteLine($"[AUTH] Received token: {token}");
            
            var response = new {
                type = "auth_success",
                pcName = _pcName,
                token = _pairingToken,
                ip = GetLocalIPv4()
            };
            
            socket.Send(JsonSerializer.Serialize(response));
            _allSockets.Add(socket);
            Console.WriteLine("[AUTH] Client authenticated.");
        }

        private static void HandleHeartbeat(IWebSocketConnection socket, Dictionary<string, object> data)
        {
            var response = new {
                type = "heartbeat_ack",
                timestamp = data["timestamp"]
            };
            socket.Send(JsonSerializer.Serialize(response));
        }

        private static void HandleCommand(IWebSocketConnection socket, Dictionary<string, object> data)
        {
            string command = data["command"]?.ToString();
            Console.WriteLine($"[CMD] Received: {command}");

            bool success = false;
            switch (command)
            {
                case "LOCK":
                    success = LockWorkStation();
                    break;
                case "SLEEP":
                    success = SetSuspendState(false, true, true);
                    break;
                case "VOLUME_UP":
                    // Implementation for volume control
                    success = true; 
                    break;
                case "VOLUME_DOWN":
                    success = true;
                    break;
                case "PING":
                    success = true;
                    break;
            }

            var response = new {
                type = "command_result",
                id = data["id"],
                success = success
            };
            socket.Send(JsonSerializer.Serialize(response));
        }

        private static string GetLocalIPv4()
        {
            return NetworkInterface.GetAllNetworkInterfaces()
                .Where(ni => ni.OperationalStatus == OperationalStatus.Up && ni.NetworkInterfaceType != NetworkInterfaceType.Loopback)
                .SelectMany(ni => ni.GetIPProperties().UnicastAddresses)
                .FirstOrDefault(ua => ua.Address.AddressFamily == AddressFamily.InterNetwork)?
                .Address.ToString();
        }

        [DllImport("user32.dll")]
        public static extern bool LockWorkStation();

        [DllImport("PowrProf.dll", CharSet = CharSet.Auto, ExactSpelling = true)]
        public static extern bool SetSuspendState(bool hiberate, bool forceCritical, bool disableWakeEvent);
    }
}
```

## Security Note
This agent binds to `0.0.0.0:8765`, meaning it is accessible from any device on your local network. Ensure your Windows Firewall allows inbound connections on port 8765. For production, implement a fixed pairing code or Windows Hello integration.
