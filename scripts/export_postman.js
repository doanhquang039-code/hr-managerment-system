const fs = require('fs');
const http = require('http');

// Vui lòng chạy lệnh: npm install openapi-to-postmanv2
const Converter = require('openapi-to-postmanv2');

const OPENAPI_URL = 'http://localhost:8080/v3/api-docs';
const OUTPUT_FILE = '../postman_collection.json';

console.log(`Đang tải OpenAPI spec từ ${OPENAPI_URL}...`);

http.get(OPENAPI_URL, (res) => {
    let data = '';

    res.on('data', (chunk) => {
        data += chunk;
    });

    res.on('end', () => {
        console.log('Tải xong. Đang chuyển đổi sang định dạng Postman...');
        
        Converter.convert({ type: 'string', data: data },
            {}, (err, conversionResult) => {
                if (err) {
                    console.error('Lỗi khi chuyển đổi:', err);
                    return;
                }
                
                if (!conversionResult.result) {
                    console.error('Không thể chuyển đổi:', conversionResult.reason);
                    return;
                }

                fs.writeFileSync(OUTPUT_FILE, JSON.stringify(conversionResult.output[0].data, null, 2));
                console.log(`Thành công! Đã lưu Postman Collection tại ${OUTPUT_FILE}`);
            }
        );
    });
}).on('error', (err) => {
    console.error('Lỗi khi kết nối đến server. Đảm bảo Spring Boot đang chạy ở cổng 8080.', err.message);
});
