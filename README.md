# OpenWrt Manager

![OpenWrt Manager](https://img.shields.io/badge/OpenWrt-Manager-blue?style=flat-square&logo=openwrt)
![Android](https://img.shields.io/badge/Android-Kotlin-green?style=flat-square&logo=android)
![Material Design 3](https://img.shields.io/badge/Material%20Design-3.0-orange?style=flat-square&logo=material-design)

Android-приложение для полноценного управления роутером на OpenWrt через LuCI ubus JSON-RPC API. Навигация через боковое меню (Navigation Drawer), локализация EN/AR (арабский + английский) с переключением языка внутри приложения.

![images](img/p6.png)
## Возможности

### Панель управления (Dashboard)
- Модель роутера, hostname, прошивка, ядро
- Uptime, загрузка CPU (load average)
- Использование оперативной памяти с прогресс-баром

### Сетевые интерфейсы
- Просмотр всех интерфейсов (LAN, WAN, WWAN, Guest и др.)
- Включение/отключение интерфейсов одним касанием
- IP-адрес, устройство, протокол
- Pull-to-refresh

### WiFi
- Список всех WiFi-сетей роутера
- Редактирование SSID, пароля, канала, мощности передатчика
- Отображение частоты, шифрования, режима
- Применение настроек через UCI + перезагрузка WiFi

### Подключённые устройства
- DHCP-аренды (MAC, IP, hostname)
- WiFi-клиенты с уровнем сигнала
- Разделение на проводные и беспроводные

### VPN
- Обнаружение OpenVPN и WireGuard подключений
- Запуск/остановка VPN одним касанием
- Отображение статуса (активно/неактивно)

### Управление пакетами (opkg)
- Список установленных пакетов
- Поиск по доступным пакетам
- Установка и удаление пакетов
- Обновление списков пакетов

### Система
- Перезагрузка роутера с подтверждением
- Выключение роутера с подтверждением

### Автообнаружение роутера
- Определение gateway через ConnectivityManager
- Параллельная проверка стандартных IP (192.168.1.1, 192.168.0.1 и др.)
- Выбор найденного роутера на экране входа

### Виджет на рабочий стол
- Компактный виджет с uptime, RAM%, количеством клиентов
- Автоматическое обновление каждые 30 минут

### Push-уведомления
- Мониторинг новых устройств в сети (WorkManager)
- Настраиваемый интервал (15/30/60 минут)
- Уведомления при подключении нового устройства

### Прочее
- Navigation Drawer с 8 разделами
- Локализация: английский (по умолчанию) + арабский; переключение языка через боковое меню (Language), поддержка RTL
- Material Design 3, тёмная и светлая темы
- Безопасное хранение учётных данных (EncryptedSharedPreferences)
- Поддержка self-signed сертификатов

## Структура проекта

```
com.example.lucimanager/
├── model/                        # Модели данных
│   ├── LoginCredentials.kt
│   ├── LuciSession.kt
│   ├── NetworkInterface.kt
│   ├── RouterInfo.kt
│   ├── WifiNetwork.kt
│   ├── ConnectedDevice.kt
│   ├── VpnConnection.kt
│   ├── OpkgPackage.kt
│   ├── DiscoveredRouter.kt
│   └── MonitoringState.kt
├── network/                      # Сетевой слой
│   └── LuciApiClient.kt         # ubus JSON-RPC клиент
├── repository/
│   └── NetworkRepository.kt     # Репозиторий данных
├── viewmodel/                    # ViewModels (MVVM)
│   ├── LoginViewModel.kt
│   ├── InterfaceViewModel.kt
│   ├── DashboardViewModel.kt
│   ├── WifiViewModel.kt
│   ├── DevicesViewModel.kt
│   ├── VpnViewModel.kt
│   └── PackagesViewModel.kt
├── ui/                           # UI
│   ├── home/HomeFragment.kt             # DrawerLayout хост
│   ├── login/LoginFragment.kt
│   ├── dashboard/DashboardFragment.kt
│   ├── interfaces/
│   │   ├── InterfacesFragment.kt
│   │   └── NetworkInterfaceAdapter.kt
│   ├── wifi/
│   │   ├── WifiFragment.kt
│   │   ├── WifiAdapter.kt
│   │   └── WifiEditDialogFragment.kt
│   ├── devices/
│   │   ├── DevicesFragment.kt
│   │   └── DeviceAdapter.kt
│   ├── vpn/
│   │   ├── VpnFragment.kt
│   │   └── VpnAdapter.kt
│   ├── packages/
│   │   ├── PackagesFragment.kt
│   │   └── PackageAdapter.kt
│   ├── system/SystemActionsFragment.kt
│   ├── settings/NotificationSettingsFragment.kt
│   └── discovery/DiscoveryDialogFragment.kt
├── service/
│   ├── RouterDiscoveryService.kt
│   ├── MonitoringWorker.kt
│   └── NotificationHelper.kt
├── widget/
│   └── RouterWidgetProvider.kt
├── utils/
│   ├── NetworkUtils.kt
│   └── ViewExtensions.kt
└── MyApplication.kt
```

## Технологии

| Категория | Технология |
|-----------|-----------|
| Язык | Kotlin |
| Архитектура | MVVM, Repository Pattern |
| UI | Material Design 3, ViewBinding, Navigation Component |
| Сеть | OkHttp, Gson, Coroutines |
| Навигация | Navigation Drawer + NavHostFragment |
| Фоновые задачи | WorkManager |
| Безопасность | EncryptedSharedPreferences |
| API | LuCI ubus JSON-RPC |

## ubus API

Приложение взаимодействует с роутером через endpoint:

```
POST http://<router_ip>/cgi-bin/luci/admin/ubus
```

Используемые ubus-вызовы:

| Объект | Метод | Назначение |
|--------|-------|-----------|
| `session` | `login` / `destroy` | Аутентификация |
| `system` | `board` / `info` | Информация о системе |
| `network.interface` | `dump` | Сетевые интерфейсы |
| `network.interface.*` | `up` / `down` | Управление интерфейсами |
| `network.wireless` | `status` | Статус WiFi |
| `uci` | `get` / `set` / `commit` | Конфигурация UCI |
| `luci-rpc` | `getDHCPLeases` | DHCP-аренды |
| `hostapd.*` | `get_clients` | WiFi-клиенты |
| `service` | `list` | Статус сервисов (VPN) |
| `file` | `exec` | Выполнение команд (opkg, reboot, wifi reload) |

## Требования

- Android 7.0+ (API 24+)
- OpenWrt роутер с включённым LuCI и ubus RPC
- Подключение к одной сети с роутером

## Сборка

```bash
git clone <repo>
cd Luci_manager
./gradlew assembleDebug
```

APK будет в `app/build/outputs/apk/debug/`.

## Разрешения

| Разрешение | Назначение |
|-----------|-----------|
| `INTERNET` | Подключение к роутеру |
| `ACCESS_NETWORK_STATE` | Проверка состояния сети |
| `ACCESS_WIFI_STATE` | Информация о WiFi |
| `ACCESS_FINE_LOCATION` | Автообнаружение роутера (Android 10+) |
| `POST_NOTIFICATIONS` | Push-уведомления (Android 13+) |

## Лицензия

MIT
