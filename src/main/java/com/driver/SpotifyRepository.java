package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name,mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Album album = new Album(title);
        albums.add(album);
        Optional<Artist> artist = findArtist(artistName);
        List<Album> albumList = artistAlbumMap.getOrDefault(artist.get(),new ArrayList<>());
        albumList.add(album);
        artistAlbumMap.put(artist.get(),albumList);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Song song = new Song(title,length);
        songs.add(song);
        Optional<Album> album = findAlbum(albumName);
        List<Song> songList = albumSongMap.getOrDefault(album.get(),new ArrayList<>());
        songList.add(song);
        albumSongMap.put(album.get(),songList);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        Optional<User> user = findUser(mobile);

        //Adding songs to playlist
        List<Song> songsListWithLength = findSongsWithSameLength(length);
        playlistSongMap.put(playlist,songsListWithLength);

        //Adding user and playlist to creator map
        creatorPlaylistMap.put(user.get(),playlist);

        //Adding user to listener of this playlist map
        List<User> userList = playlistListenerMap.getOrDefault(playlist,new ArrayList<>());
        userList.add(user.get());
        playlistListenerMap.put(playlist,userList);

        //Adding playlist to particular user
        List<Playlist> playlists1 = userPlaylistMap.getOrDefault(user.get(),new ArrayList<>());
        playlists1.add(playlist);
        userPlaylistMap.put(user.get(),playlists1);

        return playlist;

    }

    private List<Song> findSongsWithSameLength(int length) {
        List<Song> songList = new ArrayList<>();
        for(Song s : songs){
            if(s.getLength() == length) songList.add(s);
        }
        return songList;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        Optional<User> user = findUser(mobile);

        //Adding songs to playlist
        List<Song> songsListWithName = new ArrayList<>();
        for(String songName : songTitles){
            Optional<Song> song = findSongWithTitle(songName);
            if(song.isPresent()) songsListWithName.add(song.get());
        }
        playlistSongMap.put(playlist,songsListWithName);

        //Adding user and playlist to creator map
        creatorPlaylistMap.put(user.get(),playlist);

        //Adding user to listener of this playlist map
        List<User> userList = playlistListenerMap.getOrDefault(playlist,new ArrayList<>());
        userList.add(user.get());
        playlistListenerMap.put(playlist,userList);

        //Adding playlist to particular user
        List<Playlist> playlists1 = userPlaylistMap.getOrDefault(user.get(),new ArrayList<>());
        playlists1.add(playlist);
        userPlaylistMap.put(user.get(),playlists1);

        return playlist;
    }

    public Optional<Song> findSongWithTitle(String songName) {
        for(Song s : songs){
            if(s.getTitle().equals(songName)) return Optional.of(s);
        }
        return Optional.empty();
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        Optional<User> user = findUser(mobile);
        Optional<Playlist> playlist = findPlaylistInDB(playlistTitle);

        boolean userIsCreater = creatorPlaylistMap.get(user.get()).getTitle().equals(playlistTitle);
        boolean userIsListener = false;

        List<User> userOfThisPlaylist = playlistListenerMap.get(playlist.get());
        for(User u : userOfThisPlaylist){
            if(u.getName().equals(user.get().getName())){
                userIsListener = true;
                break;
            }
        }

        if(userIsListener == false && userIsCreater == false){
            userOfThisPlaylist.add(user.get());
            playlistListenerMap.put(playlist.get(),userOfThisPlaylist);
        }
        return playlist.get();
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        Optional<User> user = findUser(mobile);
        Optional<Song> song = findSongWithTitle(songTitle);

        List<User> userListForThisSong = songLikeMap.getOrDefault(song.get(),new ArrayList<>());
        boolean userAlreadyLiked = false;
        for(User u : userListForThisSong){
            if(u.getName().equals(user.get().getName())){
                userAlreadyLiked = true;
                break;
            }
        }
        if(userAlreadyLiked == false){
            userListForThisSong.add(user.get());
            songLikeMap.put(song.get(),userListForThisSong);
            song.get().setLikes(song.get().getLikes()+1);
            Album album = findAlbumWithSong(song.get());
            Artist artist = findArtistByAlbum(album);
            artist.setLikes(artist.getLikes()+1);
        }
        return song.get();
    }

    private Artist findArtistByAlbum(Album album) {
        for(Map.Entry<Artist,List<Album>> map : artistAlbumMap.entrySet()){
            List<Album> albumList = map.getValue();
            for(Album A : albumList){
                if(A.getTitle().equals(album.getTitle())){
                    return map.getKey();
                }
            }
        }
        return null;
    }

    private Album findAlbumWithSong(Song song) {
        for(Map.Entry<Album,List<Song>> map : albumSongMap.entrySet()){
            List<Song> songList = map.getValue();
            for(Song s : songList){
                if(s.getTitle().equals(song.getTitle()) && s.getLength() == song.getLength()){
                    return map.getKey();
                }
            }
        }
        return null;
    }

    public String mostPopularArtist() {
        int maxLikes = 0;
        String artistName = "";
        for(Artist A : artists){
           if(A.getLikes() > maxLikes){
               maxLikes = A.getLikes();
               artistName = A.getName();
           }
        }
        return artistName;
    }

    public String mostPopularSong() {
        int maxLikes = 0;
        String songName = "";
        for(Song s : songs){
            if(s.getLikes() > maxLikes){
                maxLikes = s.getLikes();
                songName = s.getTitle();
            }
        }
        return songName;
    }

    public Optional<Artist> findArtist(String artistName) {
        for(Artist a : artists){
            if(a.getName().equals(artistName)) return Optional.of(a);
        }
        return Optional.empty();
    }

    public Optional<Album> findAlbum(String albumName) {
        for(Album a : albums){
            if(a.getTitle().equals(albumName)){
                return Optional.of(a);
            }
        }
        return Optional.empty();
    }

    public Optional<User> findUser(String mobile) {
        for(User u : users){
            if(u.getMobile().equals(mobile)){
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }

    public Optional<Playlist> findPlaylistInDB(String playlistTitle) {
        for(Playlist p : playlists){
            if(p.getTitle().equals(playlistTitle)){
                return Optional.of(p);
            }
        }
        return Optional.empty();
    }
}
