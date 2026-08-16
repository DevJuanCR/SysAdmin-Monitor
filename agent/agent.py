import psutil
import requests
import time
import sys
import socket
import os

API_URL = os.getenv("API_URL", "http://localhost:8080/api/metrics")
INTERVALO_SEGUNDOS = int(os.getenv("INTERVALO", "5"))
HOSTNAME = os.getenv("HOSTNAME_OVERRIDE", socket.gethostname())
RUTA_DISCO = os.getenv("RUTA_DISCO", "/")

# guardamos el contador de red y el momento de la ultima lectura para calcular la velocidad
ultima_red = psutil.net_io_counters()
ultimo_momento = time.time()


def obtener_velocidad_red():
    global ultima_red, ultimo_momento
    # los contadores de psutil son acumulados asi que restamos la lectura anterior
    red = psutil.net_io_counters()
    momento = time.time()
    segundos = momento - ultimo_momento
    if segundos <= 0:
        segundos = 1
    enviado = (red.bytes_sent - ultima_red.bytes_sent) / segundos
    recibido = (red.bytes_recv - ultima_red.bytes_recv) / segundos
    ultima_red = red
    ultimo_momento = momento
    # si el contador se reinicia salen negativos, los dejamos a cero
    if enviado < 0:
        enviado = 0
    if recibido < 0:
        recibido = 0
    return round(enviado, 2), round(recibido, 2)


def obtener_metricas():
    # cogemos el porcentaje de CPU y RAM del sistema
    cpu = psutil.cpu_percent(interval=1)
    ram = psutil.virtual_memory().percent
    # el disco lo miramos en la particion que nos digan por env, por defecto la raiz
    disco = psutil.disk_usage(RUTA_DISCO).percent
    enviado, recibido = obtener_velocidad_red()
    return {"hostname": HOSTNAME, "cpuUsage": cpu, "ramUsage": ram, "diskUsage": disco,
            "netSent": enviado, "netRecv": recibido}


def enviar_metricas(metricas):
    # enviamos las metricas al backend por POST
    try:
        respuesta = requests.post(API_URL, json=metricas, timeout=5)
        if respuesta.status_code == 201:
            datos = respuesta.json()
            print(f"[OK] {datos['hostname']} - ID: {datos['id']} "
                  f"CPU: {datos['cpuUsage']}% RAM: {datos['ramUsage']}% "
                  f"Disco: {datos['diskUsage']}% "
                  f"Red: {datos['netSent']}/{datos['netRecv']} B/s")
        else:
            print(f"[WARN] Respuesta inesperada: {respuesta.status_code}")
    except requests.exceptions.ConnectionError:
        print(f"[ERROR] No se pudo conectar con {API_URL} - esta el backend arrancado?")
    except requests.exceptions.Timeout:
        print(f"[ERROR] Timeout al conectar con el servidor")
    except Exception as e:
        print(f"[ERROR] Error inesperado: {e}")


def main():
    # bucle principal que lee y envia metricas cada X segundos
    print("=" * 55)
    print("  SysAdmin Monitor Agent")
    print(f"  Host: {HOSTNAME}")
    print(f"  Enviando metricas a: {API_URL}")
    print(f"  Disco vigilado: {RUTA_DISCO}")
    print(f"  Intervalo: cada {INTERVALO_SEGUNDOS} segundos")
    print("  Pulsa Ctrl+C para detener")
    print("=" * 55)

    try:
        while True:
            metricas = obtener_metricas()
            enviar_metricas(metricas)
            time.sleep(INTERVALO_SEGUNDOS)
    except KeyboardInterrupt:
        print("\n[INFO] Agente detenido por el usuario")
        sys.exit(0)


if __name__ == "__main__":
    main()