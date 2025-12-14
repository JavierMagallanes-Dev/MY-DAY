# Mi Diario - Instrucciones de Uso 📱

## ⚠️ Importante - Primera Ejecución

Si la aplicación se cierra al iniciar sesión, sigue estos pasos:

### Solución 1: Limpiar datos de la app
1. Ve a **Ajustes** > **Aplicaciones** > **Mi Diario**
2. Selecciona **Almacenamiento**
3. Toca **Borrar datos** y **Borrar caché**
4. Abre la app nuevamente

### Solución 2: Desinstalar y reinstalar
```bash
# Desde Android Studio o terminal
./gradlew clean
./gradlew installDebug
```

### Solución 3: Desde Android Studio
1. **Build** > **Clean Project**
2. **Build** > **Rebuild Project**
3. Ejecuta la app nuevamente

## 🚀 Características de la App

### 🔐 Autenticación
- **Login** con email y contraseña
- **Sign Up** para crear cuenta nueva
- Sesión persistente (no necesitas volver a iniciar sesión)

### 👤 Perfil de Usuario
Para acceder a tu perfil:
1. En la pantalla principal, toca el botón de **menú** (⋮) en la parte superior derecha
2. Podrás editar:
   - **Nombre completo**
   - **Nombre de usuario** (@usuario)
   - **Biografía** (hasta 150 caracteres)
   - **Foto de perfil** (cámara o galería)
3. El botón **Guardar perfil** guarda tus cambios en Firestore
4. El botón **Cerrar sesión** sale de tu cuenta

### 📝 Crear Entradas de Diario
1. Toca el botón **+** (verde) en la esquina inferior derecha
2. Escribe tu título y contenido
3. Toca **Guardar**
4. Tus entradas se guardan localmente (Room) y en Firestore

### 📊 Estadísticas
La pantalla principal muestra:
- Número de entradas este año
- Días de racha consecutivos
- Palabras escritas en total

## 🎨 Tema Spotify Oscuro

La app usa un tema oscuro permanente inspirado en Spotify:
- Fondo negro (#121212)
- Acentos en verde Spotify (#1DB954)
- Texto blanco y gris claro

## 🔥 Firebase

### Firestore - Estructura de Datos
```
users/{userId}
  ├── displayName: "Juan Pérez"
  ├── username: "juanp"
  ├── email: "juan@ejemplo.com"
  ├── photoUrl: "https://..."
  ├── bio: "Amante de escribir 📝"
  └── diaries/{diaryId}
      ├── title: "Mi día increíble"
      ├── content: "Hoy fue un gran día..."
      ├── date: 1234567890
      ├── createdAt: 1234567890
      └── userId: "{userId}"
```

### Storage - Fotos de Perfil
Las fotos se guardan en: `profile_photos/{userId}_{uuid}.jpg`

## 🛠️ Comandos Útiles

### Gradle Sync
```bash
./gradlew --refresh-dependencies
```

### Limpiar y Compilar
```bash
./gradlew clean build
```

### Instalar en dispositivo
```bash
./gradlew installDebug
```

## 📱 Permisos Necesarios

La app requiere:
- ✅ **Internet** - Para Firebase
- ✅ **Almacenamiento** - Para seleccionar fotos
- ✅ **Cámara** - Para tomar fotos de perfil

## 🐛 Solución de Problemas

### La app se cierra al iniciar
- Limpia los datos de la app (ver arriba)
- Verifica que `google-services.json` esté en `app/`

### No puedo subir foto de perfil
- Verifica que hayas dado permisos de cámara y almacenamiento
- En Android 13+, los permisos se solicitan en runtime

### No se guardan mis diarios
- Verifica tu conexión a internet
- Revisa que hayas iniciado sesión correctamente
- Mira los logs en Logcat

## 📖 Flujo de Usuario

1. **Primera vez**: 
   - Login/Sign Up → HomeActivity

2. **Usuario existente**: 
   - Auto-login → HomeActivity

3. **Editar perfil**: 
   - HomeActivity → Menú (⋮) → ProfileActivity

4. **Crear entrada**: 
   - HomeActivity → Botón + → AddEditEntryActivity

5. **Ver entradas**: 
   - HomeActivity → "Todas las entradas" o "Diario" → MainActivity

## 💡 Próximas Mejoras

- [ ] Sincronización automática Room ↔ Firestore
- [ ] Carga de imagen de perfil con Glide/Coil
- [ ] Permisos en runtime para Android 6.0+
- [ ] Modo offline-first completo
- [ ] Búsqueda de entradas
- [ ] Exportar diario a PDF
- [ ] Temas personalizados

## 📞 Soporte

Si encuentras algún problema, revisa los logs en Logcat:
```
adb logcat | grep -E "MyApplicationMyDay|Firebase"
```

---
**¡Disfruta escribiendo tu diario! 📖✨**
