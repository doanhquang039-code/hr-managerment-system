# Jenkins CI/CD

File `Jenkinsfile` o thu muc goc project da cau hinh pipeline tu dong cho HR Management System.

## Yeu cau tren Jenkins agent

- JDK 21
- Git
- Docker, neu muon build hoac deploy bang Docker
- Jenkins Pipeline plugin
- Jenkins Git plugin
- Jenkins GitHub plugin, neu dung GitHub webhook

Project da co Maven Wrapper nen khong can cai Maven rieng.

## Cach tao Jenkins job

1. Tao job moi kieu `Pipeline`.
2. Chon `Pipeline script from SCM`.
3. SCM: Git.
4. Repository URL: tro den repository cua project.
5. Script Path: `Jenkinsfile`.
6. Save va bam `Build with Parameters`.

## Webhook tu GitHub vao Jenkins

Jenkinsfile da khai bao:

```groovy
triggers {
    githubPush()
    pollSCM('H/5 * * * *')
}
```

Nghia la:

- Khi GitHub gui webhook push, Jenkins tu build ngay.
- Neu webhook chua chay vi cau hinh thieu, Jenkins van tu kiem tra source moi moi 5 phut.

### Cau hinh GitHub webhook

1. Vao repository tren GitHub.
2. Chon `Settings` -> `Webhooks` -> `Add webhook`.
3. `Payload URL`:

```text
http://YOUR_JENKINS_HOST:8080/github-webhook/
```

4. `Content type`: `application/json`.
5. `Which events would you like to trigger this webhook?`: chon `Just the push event`.
6. Save.

Neu Jenkins chay o may local va GitHub khong truy cap duoc, can dung tunnel nhu ngrok/cloudflared hoac deploy Jenkins len server co public URL.

Vi du voi ngrok:

```bash
ngrok http 8080
```

Sau do dung URL HTTPS cua ngrok:

```text
https://your-ngrok-url.ngrok-free.app/github-webhook/
```

### Cau hinh trong Jenkins job

Trong job Jenkins:

1. Vao `Configure`.
2. Phan `Build Triggers`, bat `GitHub hook trigger for GITScm polling`.
3. Phan `Pipeline`, van de `Pipeline script from SCM`.
4. Save.

## Webhook tu GitLab vao Jenkins

Neu dung GitLab, cai them plugin `GitLab Plugin` tren Jenkins. Sau do webhook URL thuong co dang:

```text
http://YOUR_JENKINS_HOST:8080/project/YOUR_JOB_NAME
```

Trong GitLab:

1. Vao `Settings` -> `Webhooks`.
2. Nhap URL tren.
3. Chon `Push events`.
4. Save va bam `Test`.

## Tham so pipeline

- `RUN_TESTS`: chay test Maven truoc khi dong goi. Mac dinh `false` de build nhanh trong moi truong dev.
- `BUILD_DOCKER`: build Docker image sau khi tao file jar. Mac dinh `true`.
- `DEPLOY_LOCAL`: dung Docker de chay app truc tiep tren Jenkins agent. Mac dinh `false`.
- `APP_PORT`: port public tren may Jenkins khi `DEPLOY_LOCAL=true`. Mac dinh `8080`.

## Cac buoc pipeline

1. Checkout source code.
2. Kiem tra Maven Wrapper.
3. Compile source code.
4. Chay test neu bat `RUN_TESTS`.
5. Package file jar trong `target`.
6. Archive file jar tren Jenkins.
7. Build Docker image bang `Dockerfile.ci`: `hr-management-system:<BUILD_NUMBER>` va `hr-management-system:latest`.
8. Neu bat `DEPLOY_LOCAL`, restart container `hr-management-system`.

Luu y: stage deploy local chi tu dong deploy voi branch `main` hoac `master`. Cac branch khac van build/test/package/docker build nhung khong restart app.

## Luu y cau hinh ung dung

Neu deploy tren Jenkins agent, can dam bao moi truong chay app co cau hinh database phu hop. Nen cau hinh cac bien moi truong va thong tin nhay cam trong Jenkins Credentials hoac cau hinh runtime, khong commit vao source code.

## Bien moi truong can cho deploy Docker

Khi chay container, toi thieu can co:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `GOOGLE_CLIENT_ID`
- `GOOGLE_CLIENT_SECRET`
- `FACEBOOK_CLIENT_ID`
- `FACEBOOK_CLIENT_SECRET`

Neu chay Docker Desktop va database nam tren may host, co the dung host `host.docker.internal` thay cho `localhost`.

Vi du:

```bash
docker run -d --name hrms-jenkins-test -p 8081:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/hr_management_system?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh" \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  -e GOOGLE_CLIENT_ID=jenkins-local \
  -e GOOGLE_CLIENT_SECRET=jenkins-local \
  -e FACEBOOK_CLIENT_ID=jenkins-local \
  -e FACEBOOK_CLIENT_SECRET=jenkins-local \
  hr-management-system:jenkins-test
```
