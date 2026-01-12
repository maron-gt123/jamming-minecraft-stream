FROM python:3.12-slim

WORKDIR /app

COPY app/tiktok /app/tiktok
COPY docker/tiktok/requirements.txt /app/requirements.txt

# Pythonパッケージインストール
RUN pip install --no-cache-dir -r /app/requirements.txt

# cron_commands ディレクトリをボリュームとして mount する
VOLUME ["/app/cron_commands"]

# 実行
CMD ["python", "/app/tiktok/main.py"]
