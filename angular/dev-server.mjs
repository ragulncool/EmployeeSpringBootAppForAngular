import { createReadStream, existsSync, statSync } from 'node:fs';
import { createServer, request } from 'node:http';
import { extname, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = resolve(fileURLToPath(new URL('./dist/employee-ui/browser/', import.meta.url)));
const mimeTypes = {
  '.css': 'text/css; charset=utf-8',
  '.html': 'text/html; charset=utf-8',
  '.ico': 'image/x-icon',
  '.js': 'text/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.svg': 'image/svg+xml',
};

const server = createServer((incoming, outgoing) => {
  const requestUrl = new URL(incoming.url ?? '/', 'http://localhost');

  if (requestUrl.pathname.startsWith('/api/')) {
    const apiRequest = request(
      {
        hostname: 'localhost',
        port: 8081,
        path: requestUrl.pathname + requestUrl.search,
        method: incoming.method,
        headers: incoming.headers,
      },
      (apiResponse) => {
        outgoing.writeHead(apiResponse.statusCode ?? 502, apiResponse.headers);
        apiResponse.pipe(outgoing);
      },
    );
    apiRequest.on('error', () => {
      if (!outgoing.headersSent) {
        outgoing.writeHead(502, { 'content-type': 'application/json' });
      }
      outgoing.end(JSON.stringify({ error: 'Spring Boot API is unavailable on port 8081.' }));
    });
    incoming.pipe(apiRequest);
    return;
  }

  const requestedPath = decodeURIComponent(requestUrl.pathname);
  const filePath = resolve(root, `.${requestedPath}`);
  if (filePath !== root && !filePath.startsWith(root + sep)) {
    outgoing.writeHead(403).end('Forbidden');
    return;
  }

  const targetFile =
    existsSync(filePath) && statSync(filePath).isFile() ? filePath : resolve(root, 'index.html');
  if (!existsSync(targetFile) || !statSync(targetFile).isFile()) {
    outgoing.writeHead(503).end('Angular build not found. Run npm run build first.');
    return;
  }

  outgoing.writeHead(200, {
    'content-type': mimeTypes[extname(targetFile)] ?? 'application/octet-stream',
    'x-content-type-options': 'nosniff',
  });
  createReadStream(targetFile).pipe(outgoing);
});

server.listen(4200, '127.0.0.1', () => {
  console.log(`Angular app: http://localhost:4200 (API proxy → http://localhost:8081)`);
});
