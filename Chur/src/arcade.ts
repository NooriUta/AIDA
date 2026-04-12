/**
 * Low-level ArcadeDB HTTP client.
 * All calls go through the system-level credentials stored in config —
 * the proxy is the only party that talks to ArcadeDB directly.
 */
import { config } from './config';

export interface ArcadeResponse {
  result: unknown[];
}

export class ArcadeError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = 'ArcadeError';
  }
}

const basicAuth = Buffer.from(
  `${config.arcadeUser}:${config.arcadePass}`,
).toString('base64');

export async function arcadeCommand(
  language: 'sql' | 'cypher' | 'gremlin',
  command: string,
  params?: Record<string, unknown>,
): Promise<ArcadeResponse> {
  const url = `${config.arcadeUrl}/api/v1/command/${config.arcadeDb}`;

  const res = await fetch(url, {
    method:  'POST',
    headers: {
      'Content-Type':  'application/json',
      'Authorization': `Basic ${basicAuth}`,
    },
    body: JSON.stringify({ language, command, params: params ?? {} }),
  });

  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new ArcadeError(res.status, `ArcadeDB ${res.status}: ${text}`);
  }

  return res.json() as Promise<ArcadeResponse>;
}
