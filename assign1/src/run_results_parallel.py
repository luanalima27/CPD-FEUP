#!/usr/bin/env python3

import subprocess
import csv
import re

def compile_cpp():
    """Compila o código matrixproduct.cpp."""
    compile_cmd = ["g++", "-o", "matrixproduct", "matrixproduct.cpp", "-fopenmp", "-lpapi"]
    result = subprocess.run(compile_cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print("Erro ao compilar:", result.stderr)
        exit(1)
    print("Compilação bem-sucedida!")

def run_experiment(matrix_sizes, repetitions=5, output_file="resultados.csv"):
    """Executa o programa compilado e salva os resultados em um arquivo CSV."""

    # Criar e abrir o arquivo CSV
    with open(output_file, mode="a", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(["Tamanho da Matriz", "Execução", "Tempo (s)", "Número de Threads"])

        for size in matrix_sizes:
            print(f"\nExecutando para matriz {size}x{size}...")
            for i in range(repetitions):
                print(f"\nExecução {i+1}...")

                # Executa o programa e captura a saída
                process = subprocess.run(["./matrixproduct"],
                                         input=f"4\n0\n{size}\n0\n",
                                         text=True,
                                         capture_output=True)

                output = process.stdout
                print(output)  # Exibir saída no terminal

                # Extrair tempo e número de threads usando regex
                match_time = re.search(r"Time:\s*([\d.]+)\s*seconds", output)
                match_threads = re.search(r"Number of Threads Used:\s*(\d+)", output)

                if match_time and match_threads:
                    time = match_time.group(1)  # Captura apenas o número do tempo
                    threads = match_threads.group(1)  # Captura o número de threads
                    writer.writerow([size, i+1, time, threads])
                else:
                    print("⚠ Erro ao capturar os valores! Saída completa do programa:")
                    print(output)  # Mostrar a saída completa para debugging

    print("\n✅ Resultados salvos em", output_file)

if __name__ == "__main__":
    matrix_sizes = [3000,4096,6144,8192,10240]
    compile_cpp()
    run_experiment(matrix_sizes)

