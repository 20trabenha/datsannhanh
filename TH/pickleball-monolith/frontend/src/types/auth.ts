export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest extends LoginRequest {
  phoneNumber: string;
}

export interface LoginResponse {
  userId: number;
  token: string;
  username: string;
  role: 'ADMIN' | 'CUSTOMER';
}
