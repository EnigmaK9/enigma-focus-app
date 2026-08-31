# 🔋 Paso 5: Eficiencia Extrema (<15MB RAM y 0% Batería)
## *"Silencioso, invisible y sin desperdiciar ciclos de CPU"*

---

## 🎯 Propósito
Garantizar que el servicio en segundo plano consuma prácticamente cero batería y menos de 15 MB de RAM, funcionando de forma completamente desapercibida para el sistema operativo.

---

## 🛠️ Implementación
1. **Event-Driven puro**: Sustituir el polling periódico del watchdog por escuchas reactivas nativas de `AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED`.
2. **Cero dependencias pesadas**: Sin librerías innecesarias de analítica, sin tracking en segundo plano, sin frameworks pesados.
3. **Liberación inmediata de Compose**: Destruir y liberar de la memoria el árbol de composición de la superposición en cuanto se oculta.

---

## 💡 Principio Unix
- **Eficiencia y ligereza**: Un buen programa hace su trabajo usando la menor cantidad posible de memoria y procesador.
