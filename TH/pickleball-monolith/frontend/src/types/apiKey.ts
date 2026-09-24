export interface ApiKey {
  id: number;
  keyValue: string | null;
  ownerName: string;
  scopes: string;
  status: 'ACTIVE' | 'REVOKED';
  expiresAt: string | null;
  createdAt: string;
}

export interface ApiKeyCreateRequest {
  ownerName: string;
  validDays?: number;
}
