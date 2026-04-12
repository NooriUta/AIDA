import { describe, it, expect } from 'vitest';
import { isWriteQuery } from './rbac';

describe('isWriteQuery', () => {
  // ── Write operations — must return true ────────────────────────────────────
  it('detects INSERT', () => {
    expect(isWriteQuery('INSERT INTO User SET username = :u')).toBe(true);
  });

  it('detects UPDATE', () => {
    expect(isWriteQuery('UPDATE User SET role = :r WHERE username = :u')).toBe(true);
  });

  it('detects DELETE', () => {
    expect(isWriteQuery('DELETE FROM User WHERE username = :u')).toBe(true);
  });

  it('detects CREATE', () => {
    expect(isWriteQuery('CREATE DOCUMENT TYPE User IF NOT EXISTS')).toBe(true);
  });

  it('detects DROP', () => {
    expect(isWriteQuery('DROP TYPE User')).toBe(true);
  });

  it('detects ALTER', () => {
    expect(isWriteQuery('ALTER TYPE User ADD PROPERTY role STRING')).toBe(true);
  });

  it('detects MERGE', () => {
    expect(isWriteQuery('MERGE (n:DaliTable {id: $id}) ON CREATE SET n.name = $name')).toBe(true);
  });

  it('detects SET', () => {
    expect(isWriteQuery('SET PROPERTY User.role = "viewer"')).toBe(true);
  });

  // ── Read operations — must return false ────────────────────────────────────
  it('allows SELECT', () => {
    expect(isWriteQuery('SELECT @rid, username FROM User LIMIT 1')).toBe(false);
  });

  it('allows MATCH (Cypher)', () => {
    expect(isWriteQuery('MATCH (n:DaliTable) WHERE id(n) = $id RETURN n')).toBe(false);
  });

  it('is case-insensitive for keywords', () => {
    expect(isWriteQuery('insert into foo values (1)')).toBe(true);
    expect(isWriteQuery('select 1')).toBe(false);
  });

  it('ignores leading whitespace', () => {
    expect(isWriteQuery('  \n  INSERT INTO foo VALUES (1)')).toBe(true);
    expect(isWriteQuery('  SELECT 1')).toBe(false);
  });

  it('does not match write keywords mid-string', () => {
    // A SELECT referencing a table named "InsertLog" must not be blocked
    expect(isWriteQuery('SELECT * FROM InsertLog WHERE id = 1')).toBe(false);
  });
});
