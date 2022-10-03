import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { RNAFile } from '../model/RNAFile';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CleanService {

  private apiServerUrl: string = environment.apiBaseUrl;
  constructor(private http: HttpClient) { }

  public mergeLines(rnaFile: RNAFile): Observable<RNAFile> {
    return this.http.post<RNAFile>(`${this.apiServerUrl}/clean/merge-db-lines`, rnaFile, { withCredentials: false })
  }

  public removeHeader(rnaFile: RNAFile): Observable<RNAFile> {
    return this.http.post<RNAFile>(`${this.apiServerUrl}/clean/remove-header`, rnaFile, { withCredentials: false })
  }

  public removeLinesStartingWith(rnaFile: RNAFile, character: string): Observable<RNAFile> {
    return this.http.post<RNAFile>(`${this.apiServerUrl}/clean/remove-lines-start-with/${character[0]}`, rnaFile, { withCredentials: false })
  }

  public removeLinesContaining(rnaFile: RNAFile, word: string): Observable<RNAFile> {
    return this.http.post<RNAFile>(`${this.apiServerUrl}/clean/remove-lines-contains/${word}`, rnaFile, { withCredentials: false })
  }

  public removeWhiteSpaces(rnaFile: RNAFile): Observable<RNAFile> {
    return this.http.post<RNAFile>(`${this.apiServerUrl}/clean/remove-white-spaces`, rnaFile, { withCredentials: false })
  }
}