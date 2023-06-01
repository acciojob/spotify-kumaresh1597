package com.driver;

import java.util.*;

import org.springframework.stereotype.Service;

@Service
public class SpotifyService {

    //Auto-wire will not work in this case, no need to change this and add autowire

    SpotifyRepository spotifyRepository = new SpotifyRepository();

    public User createUser(String name, String mobile){
       return spotifyRepository.createUser(name,mobile);
    }

    public Artist createArtist(String name) {
        return spotifyRepository.createArtist(name);
    }

    public Album createAlbum(String title, String artistName) {
        Optional<Artist> artist = spotifyRepository.findArtist(artistName);
        if(artist.isPresent()) {
            return spotifyRepository.createAlbum(title,artistName);
        }
        Artist newArtist = spotifyRepository.createArtist(artistName);
        return spotifyRepository.createAlbum(title,artistName);
    }

    public Song createSong(String title, String albumName, int length) throws Exception {
        Optional<Album> album = spotifyRepository.findAlbum(albumName);
        if(album.isEmpty()){
            throw new RuntimeException("Album does not exist");
        }
        return spotifyRepository.createSong(title,albumName,length);
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Optional<User> user = spotifyRepository.findUser(mobile);
        if(user.isEmpty()){
            throw new RuntimeException("User does not exist");
        }
        return spotifyRepository.createPlaylistOnLength(mobile,title,length);
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Optional<User> user = spotifyRepository.findUser(mobile);
        if(user.isEmpty()){
            throw new RuntimeException("User does not exist");
        }
        return spotifyRepository.createPlaylistOnName(mobile,title,songTitles);
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
            Optional<User> user = spotifyRepository.findUser(mobile);
            if(user.isEmpty()){
                throw new RuntimeException("User does not exist");
            }
            Optional<Playlist> playlist = spotifyRepository.findPlaylistInDB(playlistTitle);
            if(playlist.isEmpty()){
                throw new RuntimeException("Playlist does not exist");
            }

            return spotifyRepository.findPlaylist(mobile,playlistTitle);
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        Optional<User> user = spotifyRepository.findUser(mobile);
        if(user.isEmpty()){
            throw new RuntimeException("User does not exist");
        }
        Optional<Song> song = spotifyRepository.findSongWithTitle(songTitle);
        if(song.isEmpty()){
            throw new RuntimeException("Song does not exist");
        }
        return spotifyRepository.likeSong(mobile,songTitle);
    }

    public String mostPopularArtist() {
        return spotifyRepository.mostPopularArtist();
    }

    public String mostPopularSong() {
      return spotifyRepository.mostPopularSong();
    }
}
