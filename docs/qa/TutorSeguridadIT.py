PALABRAS_PROHIBIDAS = ["contraseña", "hackear", "exploit", "bombas", "matar"]

def validar_input(usuario_input):
    for palabra in PALABRAS_PROHIBIDAS:
        if palabra in usuario_input.lower():
            return False, f"Bloqueado: Contiene '{palabra}'."
    return True, "Input válido."
