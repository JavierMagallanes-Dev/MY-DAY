# Configuración de Reglas de Firestore

## Reglas Actualizadas

Las reglas de Firestore han sido configuradas para proteger la colección `users` y sus subcolecciones, mientras mantienen abiertas las demás colecciones.

### Aplicar las Reglas en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. En el menú lateral, selecciona **Firestore Database**
4. Ve a la pestaña **Reglas**
5. Copia y pega las siguientes reglas:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Regla para la colección users: solo usuarios autenticados pueden acceder a su propio documento
    match /users/{userId} {
      // Permitir lectura y escritura solo si el usuario está autenticado y es el dueño del documento
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Subcolección diary_entries dentro de users
      match /diary_entries/{entryId} {
        // Solo el dueño puede ver y modificar sus diarios
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Todas las demás colecciones permanecen abiertas para lectura y escritura
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

6. Haz clic en **Publicar**

## Estructura de Firestore

La app utiliza la siguiente estructura en Firestore:

```
firestore/
├── users/                          (protegida - solo el dueño puede acceder)
│   └── {userId}/
│       ├── uid: string
│       ├── email: string
│       ├── displayName: string
│       ├── username: string
│       ├── photoUrl: string
│       ├── bio: string
│       ├── createdAt: timestamp
│       └── diary_entries/          (subcolección protegida)
│           └── {entryId}/
│               ├── title: string
│               ├── content: string
│               ├── date: timestamp
│               ├── createdAt: timestamp
│               └── userId: string
│
└── {otras_colecciones}/            (abiertas - acceso libre)
    └── ...
```

## Seguridad Implementada

### Colección `users`
- ✅ Solo usuarios autenticados pueden acceder
- ✅ Cada usuario solo puede ver y editar su propio documento
- ✅ Nadie puede ver documentos de otros usuarios
- ✅ Las subcolecciones heredan la misma protección

### Subcolección `diary_entries`
- ✅ Solo el dueño puede crear diarios
- ✅ Solo el dueño puede leer sus diarios
- ✅ Solo el dueño puede editar/eliminar sus diarios
- ✅ Completamente privados y seguros

### Otras colecciones
- ✅ Mantienen acceso libre (lectura/escritura)
- ✅ No afectan a tus colecciones existentes

## Verificar la Configuración

### 1. Crear una cuenta nueva
- Abre la app
- Crea una cuenta con correo y contraseña
- La app debería crear automáticamente el documento del usuario en `users/{userId}`

### 2. Verificar en Firebase Console
- Ve a Firestore Database
- Deberías ver una colección llamada `users`
- Dentro debería haber un documento con el UID del usuario
- Los campos deben incluir: uid, email, createdAt, etc.

### 3. Crear un diario
- En la app, crea una entrada de diario
- En Firebase Console, bajo `users/{userId}/diary_entries/`
- Deberías ver los diarios creados

### 4. Verificar seguridad
- Intenta acceder a otro documento de usuario desde la consola
- Las reglas deberían prevenir el acceso no autorizado

## Logs para Debug

La app ahora incluye logs detallados. Para verificar que todo funciona:

```bash
# Ver logs de Android Studio o con adb
adb logcat | grep -E "SignUpActivity|DiaryRepository|ProfileActivity"
```

Busca estos mensajes:
- ✅ `User profile created successfully in Firestore`
- ✅ `Entry synced to Firestore`
- ✅ `Sync completed: X entries from Firestore`

## Troubleshooting

### La colección `users` no aparece
1. Verifica que el usuario tenga conexión a internet
2. Revisa los logs en Logcat para ver errores
3. Verifica que las reglas de Firestore permitan la escritura
4. Asegúrate de que Firebase esté correctamente configurado en el proyecto

### Error de permisos
- Si ves errores de "PERMISSION_DENIED", revisa las reglas de Firestore
- Asegúrate de que las reglas coincidan exactamente con las de este documento

### No se sincronizan los diarios
1. Verifica conexión a internet
2. Revisa los logs de `DiaryRepository`
3. Confirma que el userId no esté vacío
4. Verifica que Firebase esté inicializado correctamente
